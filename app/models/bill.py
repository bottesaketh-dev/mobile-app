from datetime import datetime
from app.extensions import db

class Bill(db.Model):
    __tablename__ = 'bills'

    bill_id = db.Column(db.String(20), primary_key=True) # INV-DDMMYYYY-XXX
    order_id = db.Column(db.String(20), db.ForeignKey('orders.order_id'), nullable=False)
    table_id = db.Column(db.String(10), db.ForeignKey('restaurant_tables.table_id'), nullable=False)
    subtotal = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    tax_amount = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False, default=0.00) # GST
    discount_amount = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False, default=0.00)
    total_amount = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    payment_mode = db.Column(db.String(20), nullable=False) # 'cash', 'upi', 'card', 'mixed'
    payment_status = db.Column(db.String(20), default='pending', nullable=False) # 'pending', 'paid', 'partial'
    billed_by = db.Column(db.Integer, db.ForeignKey('users.user_id'), nullable=False)
    branch_id = db.Column(db.Integer, db.ForeignKey('branches.branch_id'), nullable=False)
    bill_date = db.Column(db.Date, nullable=False)
    bill_time = db.Column(db.Time, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    notes = db.Column(db.Text, nullable=True)

    # Relationships
    order = db.relationship('Order', back_populates='bills')
    table = db.relationship('RestaurantTable', back_populates='bills')
    biller = db.relationship('User', back_populates='bills_generated')
    branch = db.relationship('Branch', back_populates='bills')

    def __repr__(self):
        return f"<Bill {self.bill_id} - Total: {self.total_amount} ({self.payment_status})>"
