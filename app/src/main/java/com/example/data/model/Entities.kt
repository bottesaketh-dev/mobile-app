package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "branches")
@Serializable
data class Branch(
    @PrimaryKey val branch_id: Int,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null
)

@Entity(tableName = "grocery_categories")
@Serializable
data class GroceryCategory(
    @PrimaryKey val grocery_category_id: Int,
    val name: String,
    val description: String? = null,
    val is_active: Boolean = true
)

@Entity(tableName = "expense_categories")
@Serializable
data class ExpenseCategory(
    @PrimaryKey val expense_category_id: Int,
    val name: String,
    val description: String? = null,
    val is_active: Boolean = true
)

@Entity(tableName = "users")
@Serializable
data class User(
    @PrimaryKey val user_id: Int,
    val username: String,
    val email: String,
    val password_hash: String,
    val role: String,
    val branch_id: Int? = null,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Entity(tableName = "menu_items")
@Serializable
data class MenuItem(
    @PrimaryKey val menu_item_id: Int,
    val name: String,
    val description: String? = null,
    val category: String,
    val price: Double,
    val is_vegetarian: Boolean = true,
    val is_available: Boolean = true,
    val image_url: String? = null,
    val branch_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Entity(tableName = "restaurant_tables")
@Serializable
data class RestaurantTable(
    @PrimaryKey val table_id: String,
    val capacity: Int,
    val status: String,
    val branch_id: Int,
    val is_active: Boolean = true
)

@Entity(tableName = "grocery_items")
@Serializable
data class GroceryItem(
    @PrimaryKey val grocery_item_id: String,
    val product_name: String,
    val category_id: Int,
    val unit: String,
    val is_active: Boolean = true,
    val created_at: String? = null
)

@Entity(tableName = "employees")
@Serializable
data class Employee(
    @PrimaryKey val employee_id: String,
    val first_name: String,
    val last_name: String,
    val email: String? = null,
    val phone: String,
    val position: String,
    val monthly_salary: Double,
    val join_date: String? = null,
    val is_active: Boolean = true,
    val branch_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Entity(tableName = "orders")
@Serializable
data class Order(
    @PrimaryKey val order_id: String,
    val table_id: String,
    val status: String,
    val created_by: Int,
    val branch_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Entity(tableName = "grocery_purchases")
@Serializable
data class GroceryPurchase(
    @PrimaryKey val grocery_purchase_id: Int,
    val purchase_date: String? = null,
    val purchase_time: String? = null,
    val grocery_item_id: String,
    val quantity: Double,
    val unit_price: Double,
    val total_price: Double,
    val vendor_name: String? = null,
    val notes: String? = null,
    val recorded_by: Int,
    val branch_id: Int,
    val created_at: String? = null
)

@Entity(tableName = "expenses")
@Serializable
data class Expense(
    @PrimaryKey val expense_id: Int,
    val expense_date: String? = null,
    val expense_time: String? = null,
    val category_id: Int,
    val description: String,
    val amount: Double,
    val payment_mode: String,
    val vendor_name: String? = null,
    val receipt_number: String? = null,
    val notes: String? = null,
    val recorded_by: Int,
    val branch_id: Int,
    val created_at: String? = null
)

@Entity(tableName = "salary_payments")
@Serializable
data class SalaryPayment(
    @PrimaryKey val salary_payment_id: Int,
    val employee_id: String,
    val payment_month: Int,
    val payment_year: Int,
    val base_salary: Double,
    val bonus: Double,
    val deductions: Double,
    val net_salary: Double,
    val payment_date: String? = null,
    val payment_status: String,
    val payment_mode: String? = null,
    val processed_by: Int,
    val branch_id: Int,
    val created_at: String? = null
)

@Entity(tableName = "order_items")
@Serializable
data class OrderItem(
    @PrimaryKey val order_item_id: Int,
    val order_id: String,
    val menu_item_id: Int,
    val quantity: Int,
    val unit_price: Double,
    val total_price: Double,
    val notes: String? = null,
    val created_at: String? = null
)

@Entity(tableName = "bills")
@Serializable
data class Bill(
    @PrimaryKey val bill_id: String,
    val order_id: String,
    val table_id: String,
    val subtotal: Double,
    val tax_amount: Double,
    val discount_amount: Double,
    val total_amount: Double,
    val payment_mode: String,
    val payment_status: String,
    val billed_by: Int,
    val branch_id: Int,
    val bill_date: String? = null,
    val bill_time: String? = null,
    val created_at: String? = null,
    val notes: String? = null
)
