package com.example.data.database

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.Properties

object PostgresSyncEngine {
    private const val TAG = "PostgresSyncEngine"
    private val DB_URL: String
    private val DB_USER: String
    private val DB_PASS: String

    init {
        try {
            // Explicitly load the JDBC driver class for Android compatibility
            Class.forName("org.postgresql.Driver")
            Log.d(TAG, "PostgreSQL JDBC driver loaded successfully")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load PostgreSQL driver — sync will not work", e)
        }

        val envUrl = com.example.BuildConfig.DATABASE_URL
        if (envUrl.isNotEmpty() && (envUrl.startsWith("postgres://") || envUrl.startsWith("postgresql://"))) {
            val (jdbcUrl, user, pass) = parseDatabaseUrl(envUrl)
            DB_URL = jdbcUrl
            DB_USER = user
            DB_PASS = pass
            Log.d(TAG, "Configured PostgreSQL using DATABASE_URL from BuildConfig (host: ${DB_URL})")
        } else {
            // Fallback: hardcoded Supabase connection parameters
            DB_URL = "jdbc:postgresql://aws-1-ap-south-1.pooler.supabase.com:5432/postgres"
            DB_USER = "postgres.ofvwldyydnlowttsffwa"
            DB_PASS = "Ometrion@Genesis"
            Log.w(TAG, "DATABASE_URL not set in BuildConfig — using hardcoded fallback parameters")
        }
    }

    /**
     * Parses a postgresql:// or postgres:// connection URL into JDBC components.
     * Handles URL-encoded characters in username and password (e.g. %40 → @).
     */
    private fun parseDatabaseUrl(url: String): Triple<String, String, String> {
        val cleanUrl = url.trim()
            .removePrefix("postgresql://")
            .removePrefix("postgres://")

        // Use lastIndexOf to correctly handle '@' signs in URL-encoded passwords
        val lastAtIndex = cleanUrl.lastIndexOf('@')
        if (lastAtIndex == -1) {
            Log.w(TAG, "Could not parse DATABASE_URL — no '@' separator found")
            return Triple(url, "", "")
        }

        val userInfo = cleanUrl.substring(0, lastAtIndex)
        val hostPortDb = cleanUrl.substring(lastAtIndex + 1)

        val userParts = userInfo.split(":", limit = 2)
        // CRITICAL FIX: URL-decode the username and password
        // The Supabase URL contains %40 (encoded @) in the password
        val user = URLDecoder.decode(userParts.getOrNull(0) ?: "", "UTF-8")
        val pass = URLDecoder.decode(userParts.getOrNull(1) ?: "", "UTF-8")

        val jdbcUrl = "jdbc:postgresql://$hostPortDb"
        Log.d(TAG, "Parsed JDBC URL: $jdbcUrl, user: $user")
        return Triple(jdbcUrl, user, pass)
    }

    /**
     * Checks whether the device has an active network connection.
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Creates a JDBC connection to the Supabase PostgreSQL pooler.
     * Uses SSL (required by Supabase) and sets a 10-second login timeout.
     */
    private suspend fun getConnection(): Connection? = withContext(Dispatchers.IO) {
        try {
            DriverManager.setLoginTimeout(10)
            val props = Properties().apply {
                setProperty("user", DB_USER)
                setProperty("password", DB_PASS)
                // Supabase connection pooler requires SSL
                setProperty("ssl", "true")
                setProperty("sslmode", "require")
                // Disable certificate verification for the pooler (uses SNI)
                setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory")
                setProperty("connectTimeout", "10")
                setProperty("socketTimeout", "30")
                setProperty("ApplicationName", "BlueFoxLedger-Android")
            }
            val conn = DriverManager.getConnection(DB_URL, props)
            Log.d(TAG, "PostgreSQL connection established successfully")
            conn
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to connect to PostgreSQL: ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

    /**
     * Performs a bidirectional sync:
     * 1. PULL remote data → insert into local Room DB
     * 2. PUSH local Room data → upsert into remote PostgreSQL
     *
     * PULL runs first so that on initial install, the app immediately
     * reflects the real data from Supabase.
     */
    suspend fun syncBidirectional(context: Context) = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) {
            Log.w(TAG, "No network connection — skipping sync")
            return@withContext
        }

        val conn = getConnection()
        if (conn == null) {
            Log.e(TAG, "Could not establish DB connection — sync aborted")
            return@withContext
        }

        val db = AppDatabase.getDatabase(context)
        val dao = db.appDao()

        try {
            // ============================================================
            // STEP 1: PULL — Fetch remote data and insert into local Room
            // This runs FIRST so the app always shows live data immediately
            // ============================================================

            pullTable(conn, "branches", "SELECT * FROM branches") { rs ->
                val branch_id = rs.getInt("branch_id")
                val name = rs.getString("name") ?: ""
                val address = rs.getString("address")
                val phone = rs.getString("phone")
                val is_active = rs.getBoolean("is_active")
                val created_at = rs.getString("created_at")
                dao.insertBranch(Branch(branch_id, name, address, phone, is_active, created_at))
            }

            pullTable(conn, "grocery_categories", "SELECT * FROM grocery_categories") { rs ->
                val grocery_category_id = rs.getInt("grocery_category_id")
                val name = rs.getString("name") ?: ""
                val description = rs.getString("description")
                val is_active = rs.getBoolean("is_active")
                dao.insertGroceryCategory(GroceryCategory(grocery_category_id, name, description, is_active))
            }

            pullTable(conn, "expense_categories", "SELECT * FROM expense_categories") { rs ->
                val expense_category_id = rs.getInt("expense_category_id")
                val name = rs.getString("name") ?: ""
                val description = rs.getString("description")
                val is_active = rs.getBoolean("is_active")
                dao.insertExpenseCategory(ExpenseCategory(expense_category_id, name, description, is_active))
            }

            pullTable(conn, "users", "SELECT * FROM users") { rs ->
                val user_id = rs.getInt("user_id")
                val username = rs.getString("username") ?: ""
                val email = rs.getString("email") ?: ""
                val password_hash = rs.getString("password_hash") ?: ""
                val role = rs.getString("role") ?: "biller"
                val branch_id = rs.getObject("branch_id") as? Int
                val is_active = rs.getBoolean("is_active")
                val created_at = rs.getString("created_at")
                val updated_at = rs.getString("updated_at")
                dao.insertUser(User(user_id, username, email, password_hash, role, branch_id, is_active, created_at, updated_at))
            }

            pullTable(conn, "menu_items", "SELECT * FROM menu_items") { rs ->
                val menu_item_id = rs.getInt("menu_item_id")
                val name = rs.getString("name") ?: ""
                val description = rs.getString("description")
                val category = rs.getString("category") ?: "General"
                val price = rs.getDouble("price")
                val is_vegetarian = rs.getBoolean("is_vegetarian")
                val is_available = rs.getBoolean("is_available")
                val image_url = rs.getString("image_url")
                val branch_id = rs.getInt("branch_id")
                val created_at = rs.getString("created_at")
                val updated_at = rs.getString("updated_at")
                dao.insertMenuItem(MenuItem(menu_item_id, name, description, category, price, is_vegetarian, is_available, image_url, branch_id, created_at, updated_at))
            }

            pullTable(conn, "restaurant_tables", "SELECT * FROM restaurant_tables") { rs ->
                val table_id = rs.getString("table_id") ?: return@pullTable
                val capacity = rs.getInt("capacity")
                val status = rs.getString("status") ?: "AVAILABLE"
                val branch_id = rs.getInt("branch_id")
                val is_active = rs.getBoolean("is_active")
                dao.insertTable(RestaurantTable(table_id, capacity, status, branch_id, is_active))
            }

            pullTable(conn, "grocery_items", "SELECT * FROM grocery_items") { rs ->
                val grocery_item_id = rs.getString("grocery_item_id") ?: return@pullTable
                val product_name = rs.getString("product_name") ?: ""
                val category_id = rs.getInt("category_id")
                val unit = rs.getString("unit") ?: "unit"
                val is_active = rs.getBoolean("is_active")
                val created_at = rs.getString("created_at")
                dao.insertGroceryItem(GroceryItem(grocery_item_id, product_name, category_id, unit, is_active, created_at))
            }

            pullTable(conn, "employees", "SELECT * FROM employees") { rs ->
                val employee_id = rs.getString("employee_id") ?: return@pullTable
                val first_name = rs.getString("first_name") ?: ""
                val last_name = rs.getString("last_name") ?: ""
                val email = rs.getString("email")
                val phone = rs.getString("phone") ?: ""
                val position = rs.getString("position") ?: "Staff"
                val monthly_salary = rs.getDouble("monthly_salary")
                val join_date = rs.getString("join_date")
                val is_active = rs.getBoolean("is_active")
                val branch_id = rs.getInt("branch_id")
                val created_at = rs.getString("created_at")
                val updated_at = rs.getString("updated_at")
                dao.insertEmployee(Employee(employee_id, first_name, last_name, email, phone, position, monthly_salary, join_date, is_active, branch_id, created_at, updated_at))
            }

            pullTable(conn, "orders", "SELECT * FROM orders") { rs ->
                val order_id = rs.getString("order_id") ?: return@pullTable
                val table_id = rs.getString("table_id") ?: ""
                val status = rs.getString("status") ?: "PENDING"
                val created_by = rs.getInt("created_by")
                val branch_id = rs.getInt("branch_id")
                val created_at = rs.getString("created_at")
                val updated_at = rs.getString("updated_at")
                dao.insertOrder(Order(order_id, table_id, status, created_by, branch_id, created_at, updated_at))
            }

            pullTable(conn, "grocery_purchases", "SELECT * FROM grocery_purchases") { rs ->
                val grocery_purchase_id = rs.getInt("grocery_purchase_id")
                val purchase_date = rs.getString("purchase_date")
                val purchase_time = rs.getString("purchase_time")
                val grocery_item_id = rs.getString("grocery_item_id") ?: return@pullTable
                val quantity = rs.getDouble("quantity")
                val unit_price = rs.getDouble("unit_price")
                val total_price = rs.getDouble("total_price")
                val vendor_name = rs.getString("vendor_name")
                val notes = rs.getString("notes")
                val recorded_by = rs.getInt("recorded_by")
                val branch_id = rs.getInt("branch_id")
                val created_at = rs.getString("created_at")
                dao.insertGroceryPurchase(GroceryPurchase(grocery_purchase_id, purchase_date, purchase_time, grocery_item_id, quantity, unit_price, total_price, vendor_name, notes, recorded_by, branch_id, created_at))
            }

            pullTable(conn, "expenses", "SELECT * FROM expenses") { rs ->
                val expense_id = rs.getInt("expense_id")
                val expense_date = rs.getString("expense_date")
                val expense_time = rs.getString("expense_time")
                val category_id = rs.getInt("category_id")
                val description = rs.getString("description") ?: ""
                val amount = rs.getDouble("amount")
                val payment_mode = rs.getString("payment_mode") ?: "Cash"
                val vendor_name = rs.getString("vendor_name")
                val receipt_number = rs.getString("receipt_number")
                val notes = rs.getString("notes")
                val recorded_by = rs.getInt("recorded_by")
                val branch_id = rs.getInt("branch_id")
                val created_at = rs.getString("created_at")
                dao.insertGeneralExpense(Expense(expense_id, expense_date, expense_time, category_id, description, amount, payment_mode, vendor_name, receipt_number, notes, recorded_by, branch_id, created_at))
            }

            pullTable(conn, "salary_payments", "SELECT * FROM salary_payments") { rs ->
                val salary_payment_id = rs.getInt("salary_payment_id")
                val employee_id = rs.getString("employee_id") ?: return@pullTable
                val payment_month = rs.getInt("payment_month")
                val payment_year = rs.getInt("payment_year")
                val base_salary = rs.getDouble("base_salary")
                val bonus = rs.getDouble("bonus")
                val deductions = rs.getDouble("deductions")
                val net_salary = rs.getDouble("net_salary")
                val payment_date = rs.getString("payment_date")
                val payment_status = rs.getString("payment_status") ?: "PENDING"
                val payment_mode = rs.getString("payment_mode")
                val processed_by = rs.getInt("processed_by")
                val branch_id = rs.getInt("branch_id")
                val created_at = rs.getString("created_at")
                dao.insertSalaryPayment(SalaryPayment(salary_payment_id, employee_id, payment_month, payment_year, base_salary, bonus, deductions, net_salary, payment_date, payment_status, payment_mode, processed_by, branch_id, created_at))
            }

            pullTable(conn, "order_items", "SELECT * FROM order_items") { rs ->
                val order_item_id = rs.getInt("order_item_id")
                val order_id = rs.getString("order_id") ?: return@pullTable
                val menu_item_id = rs.getInt("menu_item_id")
                val quantity = rs.getInt("quantity")
                val unit_price = rs.getDouble("unit_price")
                val total_price = rs.getDouble("total_price")
                val notes = rs.getString("notes")
                val created_at = rs.getString("created_at")
                dao.insertOrderItem(OrderItem(order_item_id, order_id, menu_item_id, quantity, unit_price, total_price, notes, created_at))
            }

            pullTable(conn, "bills", "SELECT * FROM bills") { rs ->
                val bill_id = rs.getString("bill_id") ?: return@pullTable
                val order_id = rs.getString("order_id") ?: return@pullTable
                val table_id = rs.getString("table_id") ?: ""
                val subtotal = rs.getDouble("subtotal")
                val tax_amount = rs.getDouble("tax_amount")
                val discount_amount = rs.getDouble("discount_amount")
                val total_amount = rs.getDouble("total_amount")
                val payment_mode = rs.getString("payment_mode") ?: "Cash"
                val payment_status = rs.getString("payment_status") ?: "PENDING"
                val billed_by = rs.getInt("billed_by")
                val branch_id = rs.getInt("branch_id")
                val bill_date = rs.getString("bill_date")
                val bill_time = rs.getString("bill_time")
                val created_at = rs.getString("created_at")
                val notes = rs.getString("notes")
                dao.insertBill(Bill(bill_id, order_id, table_id, subtotal, tax_amount, discount_amount, total_amount, payment_mode, payment_status, billed_by, branch_id, bill_date, bill_time, created_at, notes))
            }

            Log.d(TAG, "PULL phase complete — local Room DB is up to date with Supabase")

            // ============================================================
            // STEP 2: PUSH — Upsert local Room data to remote PostgreSQL
            // Only pushes data created locally (e.g. new orders, bills)
            // ============================================================

            pushTable(conn, "branches", "branch_id", dao.getAllBranches().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setInt(1, item.branch_id)
                stmt.setString(2, item.name)
                stmt.setString(3, item.address)
                stmt.setString(4, item.phone)
                stmt.setBoolean(5, item.is_active)
                stmt.setString(6, item.created_at)
            }

            pushTable(conn, "grocery_categories", "grocery_category_id", dao.getAllGroceryCategories().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setInt(1, item.grocery_category_id)
                stmt.setString(2, item.name)
                stmt.setString(3, item.description)
                stmt.setBoolean(4, item.is_active)
            }

            pushTable(conn, "expense_categories", "expense_category_id", dao.getAllExpenseCategories().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setInt(1, item.expense_category_id)
                stmt.setString(2, item.name)
                stmt.setString(3, item.description)
                stmt.setBoolean(4, item.is_active)
            }

            pushTable(conn, "users", "user_id", dao.getAllUsers().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setInt(1, item.user_id)
                stmt.setString(2, item.username)
                stmt.setString(3, item.email)
                stmt.setString(4, item.password_hash)
                stmt.setString(5, item.role)
                if (item.branch_id != null) stmt.setInt(6, item.branch_id) else stmt.setNull(6, java.sql.Types.INTEGER)
                stmt.setBoolean(7, item.is_active)
                stmt.setString(8, item.created_at)
                stmt.setString(9, item.updated_at)
            }

            pushTable(conn, "menu_items", "menu_item_id", dao.getAllMenuItems().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setInt(1, item.menu_item_id)
                stmt.setString(2, item.name)
                stmt.setString(3, item.description)
                stmt.setString(4, item.category)
                stmt.setDouble(5, item.price)
                stmt.setBoolean(6, item.is_vegetarian)
                stmt.setBoolean(7, item.is_available)
                stmt.setString(8, item.image_url)
                stmt.setInt(9, item.branch_id)
                stmt.setString(10, item.created_at)
                stmt.setString(11, item.updated_at)
            }

            pushTable(conn, "restaurant_tables", "table_id", dao.getAllTables().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setString(1, item.table_id)
                stmt.setInt(2, item.capacity)
                stmt.setString(3, item.status)
                stmt.setInt(4, item.branch_id)
                stmt.setBoolean(5, item.is_active)
            }

            pushTable(conn, "grocery_items", "grocery_item_id", dao.getAllGroceryItems().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setString(1, item.grocery_item_id)
                stmt.setString(2, item.product_name)
                stmt.setInt(3, item.category_id)
                stmt.setString(4, item.unit)
                stmt.setBoolean(5, item.is_active)
                stmt.setString(6, item.created_at)
            }

            pushTable(conn, "employees", "employee_id", dao.getAllEmployees().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setString(1, item.employee_id)
                stmt.setString(2, item.first_name)
                stmt.setString(3, item.last_name)
                stmt.setString(4, item.email)
                stmt.setString(5, item.phone)
                stmt.setString(6, item.position)
                stmt.setDouble(7, item.monthly_salary)
                stmt.setString(8, item.join_date)
                stmt.setBoolean(9, item.is_active)
                stmt.setInt(10, item.branch_id)
                stmt.setString(11, item.created_at)
                stmt.setString(12, item.updated_at)
            }

            pushTable(conn, "orders", "order_id", dao.getAllOrders().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setString(1, item.order_id)
                stmt.setString(2, item.table_id)
                stmt.setString(3, item.status)
                stmt.setInt(4, item.created_by)
                stmt.setInt(5, item.branch_id)
                stmt.setString(6, item.created_at)
                stmt.setString(7, item.updated_at)
            }

            pushTable(conn, "grocery_purchases", "grocery_purchase_id", dao.getAllGroceryPurchases().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setInt(1, item.grocery_purchase_id)
                stmt.setString(2, item.purchase_date)
                stmt.setString(3, item.purchase_time)
                stmt.setString(4, item.grocery_item_id)
                stmt.setDouble(5, item.quantity)
                stmt.setDouble(6, item.unit_price)
                stmt.setDouble(7, item.total_price)
                stmt.setString(8, item.vendor_name)
                stmt.setString(9, item.notes)
                stmt.setInt(10, item.recorded_by)
                stmt.setInt(11, item.branch_id)
                stmt.setString(12, item.created_at)
            }

            pushTable(conn, "expenses", "expense_id", dao.getAllGeneralExpenses().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setInt(1, item.expense_id)
                stmt.setString(2, item.expense_date)
                stmt.setString(3, item.expense_time)
                stmt.setInt(4, item.category_id)
                stmt.setString(5, item.description)
                stmt.setDouble(6, item.amount)
                stmt.setString(7, item.payment_mode)
                stmt.setString(8, item.vendor_name)
                stmt.setString(9, item.receipt_number)
                stmt.setString(10, item.notes)
                stmt.setInt(11, item.recorded_by)
                stmt.setInt(12, item.branch_id)
                stmt.setString(13, item.created_at)
            }

            pushTable(conn, "salary_payments", "salary_payment_id", dao.getAllSalaryPayments().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setInt(1, item.salary_payment_id)
                stmt.setString(2, item.employee_id)
                stmt.setInt(3, item.payment_month)
                stmt.setInt(4, item.payment_year)
                stmt.setDouble(5, item.base_salary)
                stmt.setDouble(6, item.bonus)
                stmt.setDouble(7, item.deductions)
                stmt.setDouble(8, item.net_salary)
                stmt.setString(9, item.payment_date)
                stmt.setString(10, item.payment_status)
                stmt.setString(11, item.payment_mode)
                stmt.setInt(12, item.processed_by)
                stmt.setInt(13, item.branch_id)
                stmt.setString(14, item.created_at)
            }

            val orders = dao.getAllOrders().firstOrNull() ?: emptyList()
            for (order in orders) {
                val orderItems = dao.getBillItemsForBill(order.order_id).firstOrNull() ?: emptyList()
                pushTable(conn, "order_items", "order_item_id", orderItems) { stmt, item ->
                    stmt.setInt(1, item.order_item_id)
                    stmt.setString(2, item.order_id)
                    stmt.setInt(3, item.menu_item_id)
                    stmt.setInt(4, item.quantity)
                    stmt.setDouble(5, item.unit_price)
                    stmt.setDouble(6, item.total_price)
                    stmt.setString(7, item.notes)
                    stmt.setString(8, item.created_at)
                }
            }

            pushTable(conn, "bills", "bill_id", dao.getAllBills().firstOrNull() ?: emptyList()) { stmt, item ->
                stmt.setString(1, item.bill_id)
                stmt.setString(2, item.order_id)
                stmt.setString(3, item.table_id)
                stmt.setDouble(4, item.subtotal)
                stmt.setDouble(5, item.tax_amount)
                stmt.setDouble(6, item.discount_amount)
                stmt.setDouble(7, item.total_amount)
                stmt.setString(8, item.payment_mode)
                stmt.setString(9, item.payment_status)
                stmt.setInt(10, item.billed_by)
                stmt.setInt(11, item.branch_id)
                stmt.setString(12, item.bill_date)
                stmt.setString(13, item.bill_time)
                stmt.setString(14, item.created_at)
                stmt.setString(15, item.notes)
            }

            Log.d(TAG, "PUSH phase complete — local data synced to Supabase")

        } catch (e: Throwable) {
            Log.e(TAG, "Unexpected error during sync: ${e.javaClass.simpleName}: ${e.message}", e)
            throw e // Re-throw so AppViewModel can catch and show error message
        } finally {
            // CRITICAL FIX: Always close the connection, even if an exception occurred
            try {
                conn.close()
                Log.d(TAG, "PostgreSQL connection closed")
            } catch (closeEx: Throwable) {
                Log.w(TAG, "Error closing connection: ${closeEx.message}")
            }
        }
    }

    private suspend fun <T> pushTable(
        conn: Connection,
        tableName: String,
        primaryKeyColumn: String,
        items: List<T>,
        binder: suspend (PreparedStatement, T) -> Unit
    ) {
        if (items.isEmpty()) {
            Log.d(TAG, "Skipping push for $tableName — no local data to push")
            return
        }
        try {
            val columnMap = mapOf(
                "branches" to listOf("branch_id", "name", "address", "phone", "is_active", "created_at"),
                "grocery_categories" to listOf("grocery_category_id", "name", "description", "is_active"),
                "expense_categories" to listOf("expense_category_id", "name", "description", "is_active"),
                "users" to listOf("user_id", "username", "email", "password_hash", "role", "branch_id", "is_active", "created_at", "updated_at"),
                "menu_items" to listOf("menu_item_id", "name", "description", "category", "price", "is_vegetarian", "is_available", "image_url", "branch_id", "created_at", "updated_at"),
                "restaurant_tables" to listOf("table_id", "capacity", "status", "branch_id", "is_active"),
                "grocery_items" to listOf("grocery_item_id", "product_name", "category_id", "unit", "is_active", "created_at"),
                "employees" to listOf("employee_id", "first_name", "last_name", "email", "phone", "position", "monthly_salary", "join_date", "is_active", "branch_id", "created_at", "updated_at"),
                "orders" to listOf("order_id", "table_id", "status", "created_by", "branch_id", "created_at", "updated_at"),
                "grocery_purchases" to listOf("grocery_purchase_id", "purchase_date", "purchase_time", "grocery_item_id", "quantity", "unit_price", "total_price", "vendor_name", "notes", "recorded_by", "branch_id", "created_at"),
                "expenses" to listOf("expense_id", "expense_date", "expense_time", "category_id", "description", "amount", "payment_mode", "vendor_name", "receipt_number", "notes", "recorded_by", "branch_id", "created_at"),
                "salary_payments" to listOf("salary_payment_id", "employee_id", "payment_month", "payment_year", "base_salary", "bonus", "deductions", "net_salary", "payment_date", "payment_status", "payment_mode", "processed_by", "branch_id", "created_at"),
                "order_items" to listOf("order_item_id", "order_id", "menu_item_id", "quantity", "unit_price", "total_price", "notes", "created_at"),
                "bills" to listOf("bill_id", "order_id", "table_id", "subtotal", "tax_amount", "discount_amount", "total_amount", "payment_mode", "payment_status", "billed_by", "branch_id", "bill_date", "bill_time", "created_at", "notes")
            )

            val columnNames = columnMap[tableName] ?: run {
                Log.w(TAG, "No column map found for $tableName — skipping push")
                return
            }

            val columnsCsv = columnNames.joinToString(", ")
            val placeholders = columnNames.map { col ->
                when {
                    col.endsWith("_at") -> "?::timestamp"
                    col.endsWith("_date") || col == "join_date" -> "?::date"
                    col.endsWith("_time") -> "?::time"
                    else -> "?"
                }
            }.joinToString(", ")
            val updates = columnNames.filter { it != primaryKeyColumn }.joinToString(", ") { col ->
                when {
                    col.endsWith("_at") -> "$col = EXCLUDED.$col::timestamp"
                    col.endsWith("_date") || col == "join_date" -> "$col = EXCLUDED.$col::date"
                    col.endsWith("_time") -> "$col = EXCLUDED.$col::time"
                    else -> "$col = EXCLUDED.$col"
                }
            }

            val sql = """
                INSERT INTO $tableName ($columnsCsv) 
                VALUES ($placeholders) 
                ON CONFLICT ($primaryKeyColumn) DO UPDATE SET $updates
            """.trimIndent()

            val pstmt = conn.prepareStatement(sql)
            try {
                for (item in items) {
                    binder(pstmt, item)
                    pstmt.addBatch()
                }
                pstmt.executeBatch()
                Log.d(TAG, "Pushed ${items.size} rows to remote table: $tableName")
            } finally {
                pstmt.close()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error pushing table $tableName: ${e.javaClass.simpleName}: ${e.message}", e)
            throw e // Re-throw to propagate sync failures
        }
    }

    private suspend fun pullTable(
        conn: Connection,
        tableName: String,
        query: String,
        saver: suspend (java.sql.ResultSet) -> Unit
    ) {
        try {
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery(query)
            var count = 0
            while (rs.next()) {
                try {
                    saver(rs)
                    count++
                } catch (rowEx: Throwable) {
                    Log.w(TAG, "Skipping bad row in $tableName: ${rowEx.message}")
                }
            }
            rs.close()
            stmt.close()
            Log.d(TAG, "Pulled $count rows from remote table: $tableName")
        } catch (e: Throwable) {
            Log.e(TAG, "Error pulling table $tableName: ${e.javaClass.simpleName}: ${e.message}", e)
            // Don't re-throw — a pull failure for one table shouldn't abort the whole sync
        }
    }
}
