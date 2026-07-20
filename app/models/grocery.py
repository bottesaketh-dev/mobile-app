from datetime import datetime
from app.extensions import db

class GroceryCategory(db.Model):
    __tablename__ = 'grocery_categories'

    grocery_category_id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(50), unique=True, nullable=False)
    description = db.Column(db.Text, nullable=True)
    is_active = db.Column(db.Boolean, default=True, nullable=False)

    # Relationships
    items = db.relationship('GroceryItem', back_populates='category', lazy=True)

    def __repr__(self):
        return f"<GroceryCategory {self.name}>"


class GroceryItem(db.Model):
    __tablename__ = 'grocery_items'

    grocery_item_id = db.Column(db.String(20), primary_key=True)  # GRC-CAT-001
    product_name = db.Column(db.String(100), nullable=False)
    category_id = db.Column(db.Integer, db.ForeignKey('grocery_categories.grocery_category_id'), nullable=False)
    unit = db.Column(db.String(20), nullable=False)  # 'count', 'kg', 'grams', 'liters', 'ml', 'dozen', 'packet'
    is_active = db.Column(db.Boolean, default=True, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    # Relationships
    category = db.relationship('GroceryCategory', back_populates='items')
    purchases = db.relationship('GroceryPurchase', back_populates='grocery_item', lazy=True)

    def __repr__(self):
        return f"<GroceryItem {self.product_name} ({self.product_id})>"


class GroceryPurchase(db.Model):
    __tablename__ = 'grocery_purchases'

    grocery_purchase_id = db.Column(db.Integer, primary_key=True)
    purchase_date = db.Column(db.Date, nullable=False)
    purchase_time = db.Column(db.Time, nullable=False)
    grocery_item_id = db.Column(db.String(20), db.ForeignKey('grocery_items.grocery_item_id'), nullable=False)
    quantity = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    unit_price = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    total_price = db.Column(db.Numeric(10, 2, asdecimal=True), nullable=False)
    vendor_name = db.Column(db.String(100), nullable=True)
    notes = db.Column(db.Text, nullable=True)
    recorded_by = db.Column(db.Integer, db.ForeignKey('users.user_id'), nullable=False)
    branch_id = db.Column(db.Integer, db.ForeignKey('branches.branch_id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    # Relationships
    grocery_item = db.relationship('GroceryItem', back_populates='purchases')
    recorder = db.relationship('User', back_populates='grocery_recorded')
    branch = db.relationship('Branch', back_populates='grocery_purchases')

    def __repr__(self):
        return f"<GroceryPurchase {self.grocery_purchase_id} - Date: {self.purchase_date}, Total: {self.total_price}>"
