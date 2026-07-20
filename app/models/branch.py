from datetime import datetime
from app.extensions import db

class Branch(db.Model):
    __tablename__ = 'branches'

    branch_id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    address = db.Column(db.Text, nullable=True)
    phone = db.Column(db.String(15), nullable=True)
    is_active = db.Column(db.Boolean, default=True, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    # Relationships
    users = db.relationship('User', back_populates='branch', lazy=True)
    menu_items = db.relationship('MenuItem', back_populates='branch', lazy=True)
    tables = db.relationship('RestaurantTable', back_populates='branch', lazy=True)
    orders = db.relationship('Order', back_populates='branch', lazy=True)
    bills = db.relationship('Bill', back_populates='branch', lazy=True)
    grocery_purchases = db.relationship('GroceryPurchase', back_populates='branch', lazy=True)
    expenses = db.relationship('Expense', back_populates='branch', lazy=True)
    employees = db.relationship('Employee', back_populates='branch', lazy=True)
    salary_payments = db.relationship('SalaryPayment', back_populates='branch', lazy=True)

    def __repr__(self):
        return f"<Branch {self.name}>"
