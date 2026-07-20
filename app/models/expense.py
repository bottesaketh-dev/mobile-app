from datetime import datetime
from app.extensions import db

class ExpenseCategory(db.Model):
    __tablename__ = 'expense_categories'

    expense_category_id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(50), unique=True, nullable=False)
    description = db.Column(db.Text, nullable=True)
    is_active = db.Column(db.Boolean, default=True, nullable=False)

    # Relationships
    expenses = db.relationship('Expense', back_populates='category', lazy=True)

    def __repr__(self):
        return f"<ExpenseCategory {self.name}>"


class Expense(db.Model):
    __tablename__ = 'expenses'

    expense_id = db.Column(db.Integer, primary_key=True)
    expense_date = db.Column(db.Date, nullable=False)
    expense_time = db.Column(db.Time, nullable=False)
    category_id = db.Column(db.Integer, db.ForeignKey('expense_categories.expense_category_id'), nullable=False)
    description = db.Column(db.String(255), nullable=False)
    amount = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    payment_mode = db.Column(db.String(20), nullable=False)  # 'cash', 'upi', 'card', 'bank_transfer'
    vendor_name = db.Column(db.String(100), nullable=True)
    receipt_number = db.Column(db.String(50), nullable=True)
    notes = db.Column(db.Text, nullable=True)
    recorded_by = db.Column(db.Integer, db.ForeignKey('users.user_id'), nullable=False)
    branch_id = db.Column(db.Integer, db.ForeignKey('branches.branch_id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    # Relationships
    category = db.relationship('ExpenseCategory', back_populates='expenses')
    recorder = db.relationship('User', back_populates='expenses_recorded')
    branch = db.relationship('Branch', back_populates='expenses')

    def __repr__(self):
        return f"<Expense {self.description} - Amount: {self.amount}>"
