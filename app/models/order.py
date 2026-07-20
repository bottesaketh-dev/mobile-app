from datetime import datetime
from app.extensions import db

class Order(db.Model):
    __tablename__ = 'orders'

    order_id = db.Column(db.String(20), primary_key=True)
    table_id = db.Column(db.String(10), db.ForeignKey('restaurant_tables.table_id'), nullable=False)
    status = db.Column(db.String(20), default='active', nullable=False)  # 'active', 'completed', 'cancelled'
    created_by = db.Column(db.Integer, db.ForeignKey('users.user_id'), nullable=False)
    branch_id = db.Column(db.Integer, db.ForeignKey('branches.branch_id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    table = db.relationship('RestaurantTable', back_populates='orders')
    creator = db.relationship('User', back_populates='orders_created')
    branch = db.relationship('Branch', back_populates='orders')
    items = db.relationship('OrderItem', back_populates='order', cascade='all, delete-orphan', lazy=True)
    bills = db.relationship('Bill', back_populates='order', lazy=True)

    def __repr__(self):
        return f"<Order {self.order_id} (Status: {self.status})>"


class OrderItem(db.Model):
    __tablename__ = 'order_items'

    order_item_id = db.Column(db.Integer, primary_key=True)
    order_id = db.Column(db.String(20), db.ForeignKey('orders.order_id'), nullable=False)
    menu_item_id = db.Column(db.Integer, db.ForeignKey('menu_items.menu_item_id'), nullable=False)
    quantity = db.Column(db.Integer, nullable=False, default=1)
    unit_price = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    total_price = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    notes = db.Column(db.Text, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    # Relationships
    order = db.relationship('Order', back_populates='items')
    menu_item = db.relationship('MenuItem', back_populates='order_items')

    def __repr__(self):
        return f"<OrderItem {self.order_item_id} - Order: {self.order_id}, Item: {self.menu_item_id}, Qty: {self.quantity}>"
