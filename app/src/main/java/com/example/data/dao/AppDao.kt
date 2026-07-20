package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Branches
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: Branch): Long

    @Query("SELECT * FROM branches ORDER BY name ASC")
    fun getAllBranches(): Flow<List<Branch>>

    @Query("SELECT * FROM branches WHERE branch_id = :id")
    suspend fun getBranchById(id: Int): Branch?

    // Grocery Categories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryCategory(category: GroceryCategory): Long

    @Query("SELECT * FROM grocery_categories ORDER BY name ASC")
    fun getAllGroceryCategories(): Flow<List<GroceryCategory>>

    // Expense Categories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCategory(category: ExpenseCategory): Long

    @Query("SELECT * FROM expense_categories ORDER BY name ASC")
    fun getAllExpenseCategories(): Flow<List<ExpenseCategory>>

    // Users
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<User>>

    // Menu Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(menuItem: MenuItem): Long

    @Query("SELECT * FROM menu_items ORDER BY category, name ASC")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_items WHERE branch_id = :branchId ORDER BY category, name ASC")
    fun getMenuItemsByBranch(branchId: Int): Flow<List<MenuItem>>

    @Query("UPDATE menu_items SET is_available = :isAvailable WHERE menu_item_id = :itemId")
    suspend fun updateMenuItemAvailability(itemId: Int, isAvailable: Boolean)

    @Delete
    suspend fun deleteMenuItem(menuItem: MenuItem)

    // Restaurant Tables
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: RestaurantTable): Long

    @Query("SELECT * FROM restaurant_tables ORDER BY table_id ASC")
    fun getAllTables(): Flow<List<RestaurantTable>>

    @Query("SELECT * FROM restaurant_tables WHERE branch_id = :branchId ORDER BY table_id ASC")
    fun getTablesByBranch(branchId: Int): Flow<List<RestaurantTable>>

    @Query("UPDATE restaurant_tables SET status = :status WHERE table_id = :tableId")
    suspend fun updateTableStatus(tableId: String, status: String)

    @Query("SELECT * FROM restaurant_tables WHERE table_id = :id")
    suspend fun getTableById(id: String): RestaurantTable?

    // Grocery Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(item: GroceryItem): Long

    @Query("SELECT * FROM grocery_items ORDER BY product_name ASC")
    fun getAllGroceryItems(): Flow<List<GroceryItem>>

    // Employees
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Query("SELECT * FROM employees ORDER BY first_name, last_name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE branch_id = :branchId ORDER BY first_name, last_name ASC")
    fun getEmployeesByBranch(branchId: Int): Flow<List<Employee>>

    @Delete
    suspend fun deleteEmployee(employee: Employee)

    // Orders
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE branch_id = :branchId ORDER BY created_at DESC")
    fun getOrdersByBranch(branchId: Int): Flow<List<Order>>

    // Grocery Purchases
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryPurchase(purchase: GroceryPurchase): Long

    @Query("SELECT * FROM grocery_purchases ORDER BY created_at DESC")
    fun getAllGroceryPurchases(): Flow<List<GroceryPurchase>>

    @Query("SELECT * FROM grocery_purchases WHERE branch_id = :branchId ORDER BY created_at DESC")
    fun getGroceryPurchasesByBranch(branchId: Int): Flow<List<GroceryPurchase>>

    // Expenses
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneralExpense(expense: Expense): Long

    @Query("SELECT * FROM expenses ORDER BY created_at DESC")
    fun getAllGeneralExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE branch_id = :branchId ORDER BY created_at DESC")
    fun getGeneralExpensesByBranch(branchId: Int): Flow<List<Expense>>

    // Salary Payments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalaryPayment(payment: SalaryPayment): Long

    @Query("SELECT * FROM salary_payments ORDER BY created_at DESC")
    fun getAllSalaryPayments(): Flow<List<SalaryPayment>>

    // Order Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: OrderItem): Long

    @Query("SELECT * FROM order_items WHERE order_id = :orderId")
    fun getBillItemsForBill(orderId: String): Flow<List<OrderItem>>

    // Bills
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Query("SELECT * FROM bills ORDER BY created_at DESC")
    fun getAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE branch_id = :branchId ORDER BY created_at DESC")
    fun getBillsByBranch(branchId: Int): Flow<List<Bill>>
}
