# Flavors Ledger - Functional Overview

Flavors Ledger is a complete, multi-role restaurant management and cash-flow auditing application tailored for an Indian restaurant chain (e.g. *Flavors of India*). Its primary business objective is to track and reconcile **cash inflow** (customer billing at the tables) and **cash outflow** (grocery shopping, utility costs, and staff salaries) to compute real-time operating profitability.

---

## 👥 User Roles & Access Rights

The system enforces three distinct staff access tiers, presenting different dashboards and tabs depending on who is logged in:

### 1. Restaurant Owner (`owner`)
- **Focus**: High-level chain performance.
- **Permissions**:
  - Full view of financial reports across all branches.
  - Full control of the employee roster directory.
  - Authorizing and executing monthly salary payments.
  - Modifying the food menu catalog.

### 2. Branch Manager (`branch_manager`)
- **Focus**: Day-to-day operations at a specific location.
- **Permissions**:
  - Viewing local branch metrics.
  - Logging grocery purchases and stock procurements.
  - Recording general utility, rent, and maintenance expenses.
  - Toggling food item availability in the menu.

### 3. Biller / Cashier (`biller`)
- **Focus**: Dining floor checkouts.
- **Permissions**:
  - Accessing the Point-of-Sale (POS) dining tables grid.
  - Placing customer orders and modifying food carts.
  - Finalizing checkouts and printing PDF invoice receipts.

---

## 🗂️ Application Navigation Tabs & Features

Here is a breakdown of all navigation tabs available in the application side menu and what is inside them:

### 📊 1. Dashboard (Overview Console)
The landing page displays a summary of the current month's operations.
- **Monthly Indicators**: Highlights total Cash Inflow (sales), total Cash Outflow (expenses + groceries + payroll), and Net Cashflow (operating profit) for the month.
- **Sales Chart**: A daily line graph showing sales trajectory over the last 30 days.
- **Outflow Allocation**: A doughnut chart illustrating where the money went (groceries vs. salaries vs. utilities).
- **Recent Transaction Lists**: Split tables showing the 10 most recent customer bills and the 10 most recent opex expenses.
- **Branch Filter (Owner Only)**: A dropdown menu allowing the owner to aggregate statistics for all branches combined or filter by a specific location.

### 🍽️ 2. POS Billing (Dining Room Layout)
The main screen for the biller to manage customer orders.
- **Table Seating Grid**: Shows all dining tables with their seating capacity and color-coded statuses:
  - 🟢 **Available (Green)**: Vacant table, ready for new guests.
  - 🔴 **Occupied (Red)**: Guests are seated, and an active order ticket is open.
- **Interactive POS Interface**: Clicking an occupied table opens the checkout console:
  - **Category Pills**: Filter the food menu by category (Starters, Main Course, Breads, Rice, Beverages, Desserts).
  - **Live Cart Panel**: Click food items to add them to the table. Click `+` / `-` to adjust quantities or add special instructions.
  - **Checkout Form**: Set the payment mode (Cash, UPI, Card), input custom discount amounts, and click "Settle & Print" to generate a PDF receipt.

### 📜 3. Menu Catalog
The restaurant's digital food menu.
- **Menu Directory**: Lists all dishes with descriptions, categories, base prices, and dietary tags (Vegetarian 🟢 vs Non-Vegetarian 🔴 indicators).
- **Availability Toggle**: A switch to mark items as "In Stock" or "Out of Stock" (Managers and Owners only).
- **Add/Edit Item**: A form to add new dishes or adjust prices.

### 👥 4. Staff Directory (Employees & Payroll)
*Available to Owners only.*
- **Employee Register**: Lists all employees with contact details, hired positions (e.g. Chef, Cashier, Waiter, Cleaner), and base monthly salaries.
- **Salary Management Page**: Shows salary details for each employee. Clicking "Pay Salary" opens a payout form:
  - Select the month/year being settled.
  - Input performance bonuses or deduction penalties.
  - The system automatically calculates the Net Salary.
  - Select the payment mode (UPI, Cash, Bank Transfer) to authorize the payout.

### 🛒 5. Groceries (Raw Materials)
*Available to Owners and Managers.*
- **Grocery Logs**: A history of raw material purchases.
- **Log Purchase Form**: A form to record new grocery purchases:
  - Select an ingredient profile (e.g. Tomato, Paneer, Amul Butter block, Ghee).
  - Input purchase date, time, quantity, unit price, and vendor details.
- **Ingredient Profile Register**: A directory to register new raw stock items, defining their default packaging measurement units (kg, liters, counts, packets).

### 💸 6. General Expenses (Operating Cost Ledger)
*Available to Owners and Managers.*
- **Expenses Ledger**: Lists general operational costs separate from kitchen groceries.
- **Log General Expense Form**: Input utility bills, AC repairs, property rent, and marketing costs. Allows logging receipt tracking numbers.
- **Expense Classifications**: A settings sub-tab to create custom expense categories (e.g. Rent, Utilities, Sanitary, Crockery).

### 📈 7. Reports & Financial Analytics
*Available to Owners and Managers.*
- **Profit & Loss (P&L) Statement**: Generates a standard monthly corporate financial summary:
  - **Gross Sales (A)**: Total customer billings collected.
  - **Cost of Goods Sold (COGS) (B)**: Total money spent on raw kitchen groceries.
  - **Gross Operating Profit (C)**: Subtotal after subtracting groceries from sales ($C = A - B$).
  - **Operating Expenses (OPEX) (D)**: Total of general expenses (rent, utilities) + processed staff salaries.
  - **Net Profit (E)**: The final pre-tax net margin of the restaurant ($E = C - D$).
- **Daily Revenue Tab**: Shows a table listing day-to-day sales figures along with two visualization charts:
  - **Daily Sales Trajectory Graph**: Line graph illustrating sales fluctuations.
  - **Payment Mode Share**: A doughnut chart visualizing the percentage of sales settled via Cash vs Card vs UPI.
- **Cashflow Tab**: An overview matrix comparing overall inflow vs outflow categories.
