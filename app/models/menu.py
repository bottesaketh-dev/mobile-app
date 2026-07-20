from datetime import datetime
from app.extensions import db

class MenuItem(db.Model):
    __tablename__ = 'menu_items'

    menu_item_id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    description = db.Column(db.Text, nullable=True)
    category = db.Column(db.String(50), nullable=False)  # e.g., 'Starters', 'Main Course', 'Breads', 'Rice', 'Beverages', 'Desserts'
    price = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    is_vegetarian = db.Column(db.Boolean, default=True, nullable=False)
    is_available = db.Column(db.Boolean, default=True, nullable=False)
    image_url = db.Column(db.String(255), nullable=True)
    branch_id = db.Column(db.Integer, db.ForeignKey('branches.branch_id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)

    # Relationships
    branch = db.relationship('Branch', back_populates='menu_items')
    order_items = db.relationship('OrderItem', back_populates='menu_item', lazy=True)

    def __repr__(self):
        return f"<MenuItem {self.name} ({self.category}) - {self.price}>"
