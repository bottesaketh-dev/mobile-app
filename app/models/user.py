from datetime import datetime
from flask_login import UserMixin
from app.extensions import db, bcrypt

class User(UserMixin, db.Model):
    __tablename__ = 'users'

    user_id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(256), nullable=False)
    role = db.Column(db.String(20), nullable=False)  # 'owner', 'branch_manager', 'biller'
    branch_id = db.Column(db.Integer, db.ForeignKey('branches.branch_id'), nullable=True)
    is_active = db.Column(db.Boolean, default=True, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    branch = db.relationship('Branch', back_populates='users')
    orders_created = db.relationship('Order', back_populates='creator', lazy=True)
    bills_generated = db.relationship('Bill', back_populates='biller', lazy=True)
    grocery_recorded = db.relationship('GroceryPurchase', back_populates='recorder', lazy=True)
    expenses_recorded = db.relationship('Expense', back_populates='recorder', lazy=True)
    salaries_processed = db.relationship('SalaryPayment', back_populates='processor', lazy=True)

    def set_password(self, password):
        self.password_hash = bcrypt.generate_password_hash(password).decode('utf-8')

    def check_password(self, password):
        return bcrypt.check_password_hash(self.password_hash, password)

    def get_id(self):
        return str(self.user_id)

    @property
    def is_owner(self):
        return self.role == 'owner'

    @property
    def is_manager(self):
        return self.role == 'branch_manager'

    @property
    def is_biller(self):
        return self.role == 'biller'

    def __repr__(self):
        return f"<User {self.username} ({self.role})>"
