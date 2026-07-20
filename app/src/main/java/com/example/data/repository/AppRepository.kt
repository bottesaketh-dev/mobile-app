package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val appDao: AppDao) {
    // Branches
    val allBranches: Flow<List<Branch>> = appDao.getAllBranches()
    suspend fun insertBranch(branch: Branch): Long = appDao.insertBranch(branch)
    suspend fun getBranchById(id: Int): Branch? = appDao.getBranchById(id)

    // Grocery Categories
    val allGroceryCategories: Flow<List<GroceryCategory>> = appDao.getAllGroceryCategories()
    suspend fun insertGroceryCategory(category: GroceryCategory): Long = appDao.insertGroceryCategory(category)

    // Expense Categories
    val allExpenseCategories: Flow<List<ExpenseCategory>> = appDao.getAllExpenseCategories()
    suspend fun insertExpenseCategory(category: ExpenseCategory): Long = appDao.insertExpenseCategory(category)

    // Users
    val allUsers: Flow<List<User>> = appDao.getAllUsers()
    suspend fun insertUser(user: User): Long = appDao.insertUser(user)
    suspend fun getUserByUsername(username: String): User? = appDao.getUserByUsername(username)

    // Menu Items
    val allMenuItems: Flow<List<MenuItem>> = appDao.getAllMenuItems()
    fun getMenuItemsByBranch(branchId: Int): Flow<List<MenuItem>> = appDao.getMenuItemsByBranch(branchId)
    suspend fun insertMenuItem(menuItem: MenuItem): Long = appDao.insertMenuItem(menuItem)
    suspend fun updateMenuItemAvailability(itemId: Int, isAvailable: Boolean) = appDao.updateMenuItemAvailability(itemId, isAvailable)
    suspend fun deleteMenuItem(menuItem: MenuItem) = appDao.deleteMenuItem(menuItem)

    // Restaurant Tables
    val allTables: Flow<List<RestaurantTable>> = appDao.getAllTables()
    fun getTablesByBranch(branchId: Int): Flow<List<RestaurantTable>> = appDao.getTablesByBranch(branchId)
    suspend fun insertTable(table: RestaurantTable): Long = appDao.insertTable(table)
    suspend fun updateTableStatus(tableId: String, status: String) = appDao.updateTableStatus(tableId, status)
    suspend fun getTableById(id: String): RestaurantTable? = appDao.getTableById(id)

    // Grocery Items
    val allGroceryItems: Flow<List<GroceryItem>> = appDao.getAllGroceryItems()
    suspend fun insertGroceryItem(item: GroceryItem): Long = appDao.insertGroceryItem(item)

    // Employees
    val allEmployees: Flow<List<Employee>> = appDao.getAllEmployees()
    fun getEmployeesByBranch(branchId: Int): Flow<List<Employee>> = appDao.getEmployeesByBranch(branchId)
    suspend fun insertEmployee(employee: Employee): Long = appDao.insertEmployee(employee)
    suspend fun deleteEmployee(employee: Employee) = appDao.deleteEmployee(employee)

    // Orders
    val allOrders: Flow<List<Order>> = appDao.getAllOrders()
    fun getOrdersByBranch(branchId: Int): Flow<List<Order>> = appDao.getOrdersByBranch(branchId)
    suspend fun insertOrder(order: Order): Long = appDao.insertOrder(order)

    // Grocery Purchases
    val allGroceryPurchases: Flow<List<GroceryPurchase>> = appDao.getAllGroceryPurchases()
    fun getGroceryPurchasesByBranch(branchId: Int): Flow<List<GroceryPurchase>> = appDao.getGroceryPurchasesByBranch(branchId)
    suspend fun insertGroceryPurchase(purchase: GroceryPurchase): Long = appDao.insertGroceryPurchase(purchase)

    // Expenses
    val allGeneralExpenses: Flow<List<Expense>> = appDao.getAllGeneralExpenses()
    fun getGeneralExpensesByBranch(branchId: Int): Flow<List<Expense>> = appDao.getGeneralExpensesByBranch(branchId)
    suspend fun insertGeneralExpense(expense: Expense): Long = appDao.insertGeneralExpense(expense)

    // Salary Payments
    val allSalaryPayments: Flow<List<SalaryPayment>> = appDao.getAllSalaryPayments()
    suspend fun insertSalaryPayment(payment: SalaryPayment): Long = appDao.insertSalaryPayment(payment)

    // Order Items
    fun getBillItemsForBill(orderId: String): Flow<List<OrderItem>> = appDao.getBillItemsForBill(orderId)
    suspend fun insertOrderItem(orderItem: OrderItem): Long = appDao.insertOrderItem(orderItem)

    // Bills
    val allBills: Flow<List<Bill>> = appDao.getAllBills()
    fun getBillsByBranch(branchId: Int): Flow<List<Bill>> = appDao.getBillsByBranch(branchId)
    suspend fun insertBill(bill: Bill): Long = appDao.insertBill(bill)

    // Fallback Seed Database only if completely empty and unable to connect to remote
    suspend fun seedDatabaseIfNeeded() {
        val branches = allBranches.firstOrNull() ?: emptyList()
        if (branches.isNotEmpty()) return // Already has data locally

        // Seed a default admin/owner local login credential in case offline
        insertUser(
            User(
                user_id = 1,
                username = "admin",
                email = "admin@restaurant.com",
                password_hash = "admin123",
                role = "owner",
                branch_id = null,
                is_active = true
            )
        )
    }
}
