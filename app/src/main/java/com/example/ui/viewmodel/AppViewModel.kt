package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    // Current Session State
    val currentUser = MutableStateFlow<User?>(null)
    val selectedBranchId = MutableStateFlow<Int?>(null) // null = "All Branches" (Owner only)

    // Room Flows
    val allBranches: StateFlow<List<Branch>>
    val allUsers: StateFlow<List<User>>
    val allTables: StateFlow<List<RestaurantTable>>
    val allMenuItems: StateFlow<List<MenuItem>>
    val allBills: StateFlow<List<Bill>>
    val allEmployees: StateFlow<List<Employee>>
    val allSalaryPayments: StateFlow<List<SalaryPayment>>
    val allGroceryCategories: StateFlow<List<GroceryCategory>>
    val allExpenseCategories: StateFlow<List<ExpenseCategory>>
    val allGroceryItems: StateFlow<List<GroceryItem>>
    val allGroceryPurchases: StateFlow<List<GroceryPurchase>>
    val allGeneralExpenses: StateFlow<List<Expense>>
    val allOrders: StateFlow<List<Order>>

    // POS Cart State
    val posCart = MutableStateFlow<Map<MenuItem, Int>>(emptyMap())
    val posCartNotes = MutableStateFlow<Map<Int, String>>(emptyMap()) // menuItemId -> notes
    val selectedTable = MutableStateFlow<RestaurantTable?>(null)
    val posCategoryFilter = MutableStateFlow("Starters")

    // UI Action Feedback (Status Messages)
    val uiMessage = MutableStateFlow<String?>(null)

    // Chat Message State
    val chatMessages = MutableStateFlow<List<Pair<String, String>>>(listOf(
        "assistant" to "Welcome to BlueFox Ledger AI Command Center! I have real-time access to your cash-flow registry. Ask me things like:\n• *'What is our total revenue?'*\n• *'How many tables are occupied?'*\n• *'Summarize our operating profitability (P&L)'*\n• *'Who is our highest paid chef?'*"
    ))
    val isChatTyping = MutableStateFlow(false)
    val isSyncing = MutableStateFlow(false)
    // True once the very first sync attempt has completed (success or failure)
    val initialSyncDone = MutableStateFlow(false)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())

        // Initializing StateFlows
        allBranches = repository.allBranches.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allTables = repository.allTables.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allMenuItems = repository.allMenuItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allBills = repository.allBills.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allEmployees = repository.allEmployees.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allSalaryPayments = repository.allSalaryPayments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allGroceryCategories = repository.allGroceryCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allExpenseCategories = repository.allExpenseCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allGroceryItems = repository.allGroceryItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allGroceryPurchases = repository.allGroceryPurchases.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allGeneralExpenses = repository.allGeneralExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allOrders = repository.allOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Database seeding and remote sync
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
            triggerSync(isInitialSync = true)
        }
    }

    fun triggerSync(isInitialSync: Boolean = false) {
        viewModelScope.launch {
            isSyncing.value = true
            try {
                com.example.data.database.PostgresSyncEngine.syncBidirectional(getApplication())
                if (!isInitialSync) {
                    uiMessage.value = "☁️ Cloud Sync Complete!"
                }
            } catch (e: Throwable) {
                Log.e("AppViewModel", "Sync failed: ${e.message}", e)
                val msg = when {
                    e.message?.contains("network", ignoreCase = true) == true -> "Sync Failed: No network connection"
                    e.message?.contains("password", ignoreCase = true) == true -> "Sync Failed: DB authentication error"
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Sync Failed: Connection timed out"
                    else -> "Sync Failed: ${e.message?.take(60)}"
                }
                uiMessage.value = msg
            } finally {
                isSyncing.value = false
                if (isInitialSync) initialSyncDone.value = true
            }
        }
    }

    // Helper functions to get branch name
    fun getBranchName(branchId: Int): String {
        return allBranches.value.find { it.branch_id == branchId }?.name ?: "Unknown Branch"
    }

    // Login Action
    fun login(username: String, passwordChar: String): Boolean {
        val user = allUsers.value.find { it.username == username && it.password_hash == passwordChar }
        return if (user != null) {
            currentUser.value = user
            selectedBranchId.value = if (user.role == "owner") null else user.branch_id
            uiMessage.value = "Welcome back, ${user.username} (${user.role.uppercase()})!"
            true
        } else {
            // Check standard hardcoded fallbacks if DB isn't synced yet
            if (username == "owner" && passwordChar == "owner123") {
                currentUser.value = User(1, "owner", "owner@flavors.com", "owner123", "owner", null)
                selectedBranchId.value = null
                uiMessage.value = "Logged in as Owner (Offline Fallback)"
                true
            } else if (username == "admin" && passwordChar == "admin123") {
                currentUser.value = User(1, "admin", "admin@flavors.com", "admin123", "owner", null)
                selectedBranchId.value = null
                uiMessage.value = "Logged in as Admin"
                true
            } else {
                uiMessage.value = "Invalid username or password!"
                false
            }
        }
    }

    // Bypass login for demonstration / setup
    fun bypassLogin(role: String) {
        val branchId = allBranches.value.firstOrNull()?.branch_id ?: 1
        currentUser.value = when (role) {
            "owner" -> User(user_id = 1, username = "owner", email = "owner@flavors.com", password_hash = "owner123", role = "owner", branch_id = null)
            "branch_manager" -> User(user_id = 2, username = "manager", email = "manager@flavors.com", password_hash = "manager123", role = "branch_manager", branch_id = branchId)
            else -> User(user_id = 3, username = "biller", email = "biller@flavors.com", password_hash = "biller123", role = "biller", branch_id = branchId)
        }
        selectedBranchId.value = if (role == "owner") null else branchId
        uiMessage.value = "Logged in as ${role.replace("_", " ").uppercase()}"
    }

    fun logout() {
        currentUser.value = null
        selectedBranchId.value = null
        posCart.value = emptyMap()
        posCartNotes.value = emptyMap()
        selectedTable.value = null
        uiMessage.value = "Successfully logged out!"
    }

    // POS Cart Actions
    fun selectTable(table: RestaurantTable) {
        selectedTable.value = table
        posCart.value = emptyMap()
        posCartNotes.value = emptyMap()
        if (table.status == "AVAILABLE") {
            viewModelScope.launch {
                repository.updateTableStatus(table.table_id, "OCCUPIED")
                selectedTable.value = table.copy(status = "OCCUPIED")
            }
        }
    }

    fun releaseTable() {
        val table = selectedTable.value ?: return
        viewModelScope.launch {
            repository.updateTableStatus(table.table_id, "AVAILABLE")
            selectedTable.value = null
            posCart.value = emptyMap()
            posCartNotes.value = emptyMap()
        }
    }

    fun addToCart(item: MenuItem) {
        val currentCart = posCart.value.toMutableMap()
        currentCart[item] = (currentCart[item] ?: 0) + 1
        posCart.value = currentCart
    }

    fun removeFromCart(item: MenuItem) {
        val currentCart = posCart.value.toMutableMap()
        val count = currentCart[item] ?: return
        if (count > 1) {
            currentCart[item] = count - 1
        } else {
            currentCart.remove(item)
            val currentNotes = posCartNotes.value.toMutableMap()
            currentNotes.remove(item.menu_item_id)
            posCartNotes.value = currentNotes
        }
        posCart.value = currentCart
    }

    fun updateCartNotes(itemId: Int, notes: String) {
        val currentNotes = posCartNotes.value.toMutableMap()
        currentNotes[itemId] = notes
        posCartNotes.value = currentNotes
    }

    fun settleBill(discount: Double, paymentMode: String) {
        val table = selectedTable.value ?: return
        val cartItems = posCart.value
        if (cartItems.isEmpty()) {
            uiMessage.value = "Cart is empty!"
            return
        }

        viewModelScope.launch {
            val total = cartItems.entries.sumOf { it.key.price * it.value }
            val finalAmount = (total - discount).coerceAtLeast(0.0)
            val branchId = table.branch_id
            val orderId = "ORD_" + System.currentTimeMillis()
            val billId = "BILL_" + System.currentTimeMillis()

            // 1. Insert Order
            repository.insertOrder(
                Order(
                    order_id = orderId,
                    table_id = table.table_id,
                    status = "COMPLETED",
                    created_by = currentUser.value?.user_id ?: 1,
                    branch_id = branchId,
                    created_at = java.time.Instant.now().toString(),
                    updated_at = java.time.Instant.now().toString()
                )
            )

            // 2. Insert Order Items
            var orderItemCounter = 1
            for ((item, qty) in cartItems) {
                repository.insertOrderItem(
                    OrderItem(
                        order_item_id = ((System.currentTimeMillis() / 1000).toInt() + orderItemCounter++).coerceAtLeast(1),
                        order_id = orderId,
                        menu_item_id = item.menu_item_id,
                        quantity = qty,
                        unit_price = item.price,
                        total_price = item.price * qty,
                        notes = posCartNotes.value[item.menu_item_id] ?: "",
                        created_at = java.time.Instant.now().toString()
                    )
                )
            }

            // 3. Insert Bill
            repository.insertBill(
                Bill(
                    bill_id = billId,
                    order_id = orderId,
                    table_id = table.table_id,
                    subtotal = total,
                    tax_amount = total * 0.05, // 5% GST
                    discount_amount = discount,
                    total_amount = finalAmount,
                    payment_mode = paymentMode,
                    payment_status = "PAID",
                    billed_by = currentUser.value?.user_id ?: 1,
                    branch_id = branchId,
                    bill_date = java.time.LocalDate.now().toString(),
                    bill_time = java.time.LocalTime.now().toString(),
                    created_at = java.time.Instant.now().toString(),
                    notes = "Checkout from App"
                )
            )

            // 4. Set table available
            repository.updateTableStatus(table.table_id, "AVAILABLE")
            selectedTable.value = null
            posCart.value = emptyMap()
            posCartNotes.value = emptyMap()
            uiMessage.value = "Invoice #$billId Settle Complete! Collected: ₹$finalAmount"
            triggerSync()
        }
    }

    // Menu Actions
    fun addMenuItem(name: String, desc: String, price: Double, category: String, isVeg: Boolean) {
        val branchId = selectedBranchId.value ?: allBranches.value.firstOrNull()?.branch_id ?: 1
        viewModelScope.launch {
            repository.insertMenuItem(
                MenuItem(
                    menu_item_id = (System.currentTimeMillis() / 1000).toInt(),
                    name = name,
                    description = desc,
                    price = price,
                    category = category,
                    is_vegetarian = isVeg,
                    is_available = true,
                    branch_id = branchId,
                    created_at = java.time.Instant.now().toString(),
                    updated_at = java.time.Instant.now().toString()
                )
            )
            uiMessage.value = "Dish '$name' added successfully!"
            triggerSync()
        }
    }

    fun toggleMenuItemAvailability(item: MenuItem) {
        viewModelScope.launch {
            repository.updateMenuItemAvailability(item.menu_item_id, !item.is_available)
            uiMessage.value = "Availability updated for ${item.name}!"
            triggerSync()
        }
    }

    fun deleteMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
            uiMessage.value = "Dish '${item.name}' removed from menu."
            triggerSync()
        }
    }

    // Staff & Payroll Actions
    fun addEmployee(firstName: String, lastName: String, email: String, phone: String, position: String, baseSalary: Double) {
        val branchId = selectedBranchId.value ?: allBranches.value.firstOrNull()?.branch_id ?: 1
        viewModelScope.launch {
            repository.insertEmployee(
                Employee(
                    employee_id = "EMP_" + System.currentTimeMillis(),
                    first_name = firstName,
                    last_name = lastName,
                    email = email.ifEmpty { null },
                    phone = phone,
                    position = position,
                    monthly_salary = baseSalary,
                    join_date = java.time.LocalDate.now().toString(),
                    is_active = true,
                    branch_id = branchId,
                    created_at = java.time.Instant.now().toString(),
                    updated_at = java.time.Instant.now().toString()
                )
            )
            uiMessage.value = "Employee '$firstName $lastName' registered successfully!"
            triggerSync()
        }
    }

    fun paySalary(employeeId: String, employeeName: String, month: Int, year: Int, bonus: Double, deduction: Double, baseSalary: Double, paymentMode: String) {
        val branchId = selectedBranchId.value ?: allBranches.value.firstOrNull()?.branch_id ?: 1
        viewModelScope.launch {
            val netSalary = baseSalary + bonus - deduction
            repository.insertSalaryPayment(
                SalaryPayment(
                    salary_payment_id = (System.currentTimeMillis() / 1000).toInt(),
                    employee_id = employeeId,
                    payment_month = month,
                    payment_year = year,
                    base_salary = baseSalary,
                    bonus = bonus,
                    deductions = deduction,
                    net_salary = netSalary,
                    payment_date = java.time.LocalDate.now().toString(),
                    payment_status = "PAID",
                    payment_mode = paymentMode,
                    processed_by = currentUser.value?.user_id ?: 1,
                    branch_id = branchId,
                    created_at = java.time.Instant.now().toString()
                )
            )
            uiMessage.value = "Authorised ₹$netSalary payout to $employeeName for $month/$year."
            triggerSync()
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
            uiMessage.value = "Employee '${employee.first_name} ${employee.last_name}' removed from registry."
            triggerSync()
        }
    }

    // Grocery Actions
    fun addGroceryCategory(name: String, description: String) {
        viewModelScope.launch {
            repository.insertGroceryCategory(
                GroceryCategory(
                    grocery_category_id = (System.currentTimeMillis() / 1000).toInt(),
                    name = name,
                    description = description.ifEmpty { null },
                    is_active = true
                )
            )
            uiMessage.value = "Grocery Category '$name' added."
            triggerSync()
        }
    }

    fun addGroceryItem(productName: String, categoryId: Int, unit: String) {
        viewModelScope.launch {
            repository.insertGroceryItem(
                GroceryItem(
                    grocery_item_id = "GI_" + System.currentTimeMillis(),
                    product_name = productName,
                    category_id = categoryId,
                    unit = unit,
                    is_active = true,
                    created_at = java.time.Instant.now().toString()
                )
            )
            uiMessage.value = "Grocery Item '$productName' registered."
            triggerSync()
        }
    }

    fun logGroceryPurchase(groceryItemId: String, quantity: Double, unitPrice: Double, vendorName: String, notes: String) {
        val branchId = selectedBranchId.value ?: allBranches.value.firstOrNull()?.branch_id ?: 1
        viewModelScope.launch {
            val total = quantity * unitPrice
            repository.insertGroceryPurchase(
                GroceryPurchase(
                    grocery_purchase_id = (System.currentTimeMillis() / 1000).toInt(),
                    purchase_date = java.time.LocalDate.now().toString(),
                    purchase_time = java.time.LocalTime.now().toString(),
                    grocery_item_id = groceryItemId,
                    quantity = quantity,
                    unit_price = unitPrice,
                    total_price = total,
                    vendor_name = vendorName.ifEmpty { null },
                    notes = notes.ifEmpty { null },
                    recorded_by = currentUser.value?.user_id ?: 1,
                    branch_id = branchId,
                    created_at = java.time.Instant.now().toString()
                )
            )
            uiMessage.value = "Logged ₹$total purchase of grocery."
            triggerSync()
        }
    }

    // Expense Actions
    fun addExpenseCategory(name: String, description: String) {
        viewModelScope.launch {
            repository.insertExpenseCategory(
                ExpenseCategory(
                    expense_category_id = (System.currentTimeMillis() / 1000).toInt(),
                    name = name,
                    description = description.ifEmpty { null },
                    is_active = true
                )
            )
            uiMessage.value = "Expense Category '$name' added."
            triggerSync()
        }
    }

    fun logGeneralExpense(categoryId: Int, amount: Double, description: String, paymentMode: String, vendorName: String, receiptNumber: String, notes: String) {
        val branchId = selectedBranchId.value ?: allBranches.value.firstOrNull()?.branch_id ?: 1
        viewModelScope.launch {
            repository.insertGeneralExpense(
                Expense(
                    expense_id = (System.currentTimeMillis() / 1000).toInt(),
                    expense_date = java.time.LocalDate.now().toString(),
                    expense_time = java.time.LocalTime.now().toString(),
                    category_id = categoryId,
                    description = description,
                    amount = amount,
                    payment_mode = paymentMode,
                    vendor_name = vendorName.ifEmpty { null },
                    receipt_number = receiptNumber.ifEmpty { null },
                    notes = notes.ifEmpty { null },
                    recorded_by = currentUser.value?.user_id ?: 1,
                    branch_id = branchId,
                    created_at = java.time.Instant.now().toString()
                )
            )
            uiMessage.value = "Logged operating expense of ₹$amount."
            triggerSync()
        }
    }

    // AI Command Center Chat Processor
    fun sendChatMessage(msg: String) {
        if (msg.trim().isEmpty()) return
        val currentList = chatMessages.value.toMutableList()
        currentList.add("user" to msg)
        chatMessages.value = currentList

        isChatTyping.value = true

        viewModelScope.launch {
            val branchFilter = selectedBranchId.value
            val bills = allBills.value.filter { branchFilter == null || it.branch_id == branchFilter }
            val opexExpenses = allGeneralExpenses.value.filter { branchFilter == null || it.branch_id == branchFilter }
            val groceryPurchases = allGroceryPurchases.value.filter { branchFilter == null || it.branch_id == branchFilter }
            val staff = allEmployees.value.filter { branchFilter == null || it.branch_id == branchFilter }
            val tables = allTables.value.filter { branchFilter == null || it.branch_id == branchFilter }
            val menu = allMenuItems.value.filter { branchFilter == null || it.branch_id == branchFilter }

            val totalSales = bills.sumOf { it.total_amount }
            val totalGroceries = groceryPurchases.sumOf { it.total_price }
            val grossProfit = totalSales - totalGroceries
            val generalOpexVal = opexExpenses.sumOf { it.amount }
            val salariesPaid = allSalaryPayments.value.filter { branchFilter == null || it.branch_id == branchFilter }.sumOf { it.net_salary }
            val totalOpex = generalOpexVal + salariesPaid
            val netProfit = grossProfit - totalOpex
            val occupiedTables = tables.count { it.status == "OCCUPIED" }
            val totalTables = tables.size

            val systemPrompt = """
                You are the BlueFox Ledger AI Assistant, a professional, production-grade financial auditor and operations manager for the restaurant chain.
                You are given real-time access to the live restaurant database metrics below. Use this context to answer the user's questions with absolute accuracy. Always format currency in Indian Rupees (e.g. ₹1,25,000.00). Keep your answers crisp, professional, and visually formatted using markdown.

                === CURRENT REAL-TIME METRICS ===
                - Selected Branch: ${if (branchFilter == null) "All Branches" else getBranchName(branchFilter)}
                - Total Revenue (Gross Sales): ₹${String.format("%.2f", totalSales)} (${bills.size} successful checkouts)
                - Cost of Goods Sold (COGS - Kitchen Groceries): ₹${String.format("%.2f", totalGroceries)}
                - Gross Operating Profit: ₹${String.format("%.2f", grossProfit)}
                - Operating Expenses (Utilities, Rent, Salaries): ₹${String.format("%.2f", totalOpex)}
                  * Utilities/Rent: ₹${String.format("%.2f", generalOpexVal)}
                  * Staff Payroll: ₹${String.format("%.2f", salariesPaid)}
                - Net Operating Profit: ₹${String.format("%.2f", netProfit)}
                - Net Operating Profit Margin: ${if (totalSales > 0) String.format("%.1f%%", (netProfit / totalSales) * 100) else "0.0%"}
                - Seating Occupancy: $occupiedTables of $totalTables tables occupied (${totalTables - occupiedTables} vacant)
                - Staff Size: ${staff.size} active employees
                - Menu Catalog: ${menu.size} dishes registered
                
                === STAFF REGISTRY DETAILS ===
                ${staff.take(15).joinToString("\n") { "• ${it.first_name} ${it.last_name} - ${it.position} (Base Salary: ₹${it.monthly_salary})" }}
                
                === MENU CATALOG DETAILS ===
                ${menu.take(15).joinToString("\n") { "• ${it.name} (${it.category}) - ₹${it.price} [Veg: ${it.is_vegetarian}, Available: ${it.is_available}]" }}
                
                Be helpful, concise, and professional. If the user asks general questions about the business, explain how these real-time numbers impact their bottom line.
            """.trimIndent()

            val reply = GitHubModelsClient.getAiResponse(systemPrompt, msg)
            
            val updatedList = chatMessages.value.toMutableList()
            updatedList.add("assistant" to reply)
            chatMessages.value = updatedList
            isChatTyping.value = false
        }
    }
}
