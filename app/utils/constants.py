# User roles
ROLES = ('owner', 'branch_manager', 'biller')

# Categories
MENU_CATEGORIES = ('Starters', 'Main Course', 'Breads', 'Rice', 'Beverages', 'Desserts')

GROCERY_CATEGORIES = (
    'Vegetables', 'Fruits', 'Meat', 'Pulses', 'Dairy Products', 
    'Spices', 'Oils', 'Grains', 'Beverages', 'Others'
)

EXPENSE_CATEGORIES = (
    'Utensils', 'Sanitary Products', 'Crockery', 'Maintenance', 
    'Utilities', 'Rent', 'Marketing', 'Miscellaneous'
)

# Measurement Units
UNITS = ('count', 'kg', 'grams', 'liters', 'ml', 'dozen', 'packet')

# Payment Modes
BILL_PAYMENT_MODES = ('cash', 'upi', 'card', 'mixed')
EXPENSE_PAYMENT_MODES = ('cash', 'upi', 'card', 'bank_transfer')
SALARY_PAYMENT_MODES = ('cash', 'bank_transfer', 'upi')

# GST rates (Food: 5%, Beverages: 18%)
GST_RATES = {
    'Starters': 0.05,
    'Main Course': 0.05,
    'Breads': 0.05,
    'Rice': 0.05,
    'Beverages': 0.18,
    'Desserts': 0.05
}

DEFAULT_GST_RATE = 0.05
