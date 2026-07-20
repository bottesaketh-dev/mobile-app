from datetime import datetime
from app.extensions import db

class Employee(db.Model):
    __tablename__ = 'employees'

    employee_id = db.Column(db.String(20), primary_key=True) # EMP-001
    first_name = db.Column(db.String(50), nullable=False)
    last_name = db.Column(db.String(50), nullable=False)
    email = db.Column(db.String(120), nullable=True)
    phone = db.Column(db.String(15), nullable=False)
    position = db.Column(db.String(50), nullable=False)  # Chef, Waiter, Cleaner, Cashier, Manager
    monthly_salary = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    join_date = db.Column(db.Date, nullable=False)
    is_active = db.Column(db.Boolean, default=True, nullable=False)
    branch_id = db.Column(db.Integer, db.ForeignKey('branches.branch_id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    branch = db.relationship('Branch', back_populates='employees')
    salary_payments = db.relationship('SalaryPayment', back_populates='employee', cascade='all, delete-orphan', lazy=True)

    @property
    def full_name(self):
        return f"{self.first_name} {self.last_name}"

    def __repr__(self):
        return f"<Employee {self.full_name} ({self.employee_id})>"


class SalaryPayment(db.Model):
    __tablename__ = 'salary_payments'

    salary_payment_id = db.Column(db.Integer, primary_key=True)
    employee_id = db.Column(db.String(20), db.ForeignKey('employees.employee_id'), nullable=False)
    payment_month = db.Column(db.Integer, nullable=False)  # 1 to 12
    payment_year = db.Column(db.Integer, nullable=False)
    base_salary = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    bonus = db.Column(db.Numeric(10, 2, asdecimal=True), default=0.00, nullable=False)
    deductions = db.Column(db.Numeric(10, 2, asdecimal=True), default=0.00, nullable=False)
    net_salary = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    payment_date = db.Column(db.Date, nullable=True)
    payment_status = db.Column(db.String(20), default='pending', nullable=False) # 'pending', 'paid'
    payment_mode = db.Column(db.String(20), nullable=True)  # 'cash', 'bank_transfer', 'upi'
    processed_by = db.Column(db.Integer, db.ForeignKey('users.user_id'), nullable=False)
    branch_id = db.Column(db.Integer, db.ForeignKey('branches.branch_id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    # Relationships
    employee = db.relationship('Employee', back_populates='salary_payments')
    processor = db.relationship('User', back_populates='salaries_processed')
    branch = db.relationship('Branch', back_populates='salary_payments')

    def __repr__(self):
        return f"<SalaryPayment {self.id} - Emp: {self.employee_id}, Period: {self.payment_month}/{self.payment_year}, Status: {self.payment_status}>"
