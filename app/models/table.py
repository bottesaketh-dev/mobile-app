from app.extensions import db

class RestaurantTable(db.Model):
    __tablename__ = 'restaurant_tables'

    table_id = db.Column(db.String(10), primary_key=True)
    capacity = db.Column(db.Integer, nullable=False, default=4)
    status = db.Column(db.String(20), default='available', nullable=False)  # 'available', 'occupied', 'reserved'
    branch_id = db.Column(db.Integer, db.ForeignKey('branches.branch_id'), nullable=False)
    is_active = db.Column(db.Boolean, default=True, nullable=False)

    # Relationships
    branch = db.relationship('Branch', back_populates='tables')
    orders = db.relationship('Order', back_populates='table', lazy=True)
    bills = db.relationship('Bill', back_populates='table', lazy=True)

    def __repr__(self):
        return f"<RestaurantTable {self.table_id} (Capacity: {self.capacity}, Status: {self.status})>"
