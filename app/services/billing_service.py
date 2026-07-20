from datetime import datetime, date
from decimal import Decimal
from app.extensions import db
from app.models.order import Order, OrderItem
from app.models.table import RestaurantTable
from app.models.menu import MenuItem
from app.models.bill import Bill
from app.utils.helpers import get_ist_now, get_ist_date, get_ist_time, generate_order_number, generate_bill_number
from app.utils.constants import GST_RATES, DEFAULT_GST_RATE

class BillingService:
    @staticmethod
    def get_orders_today_count(branch_id):
        today = get_ist_date()
        start_of_today = datetime.combine(today, datetime.min.time())
        end_of_today = datetime.combine(today, datetime.max.time())
        return Order.query.filter(
            Order.branch_id == branch_id,
            Order.created_at >= start_of_today,
            Order.created_at <= end_of_today
        ).count()

    @staticmethod
    def get_bills_today_count(branch_id):
        today = get_ist_date()
        start_of_today = datetime.combine(today, datetime.min.time())
        end_of_today = datetime.combine(today, datetime.max.time())
        return Bill.query.filter(
            Bill.branch_id == branch_id,
            Bill.created_at >= start_of_today,
            Bill.created_at <= end_of_today
        ).count()

    @staticmethod
    def create_order(table_id, user_id, branch_id):
        table = db.session.get(RestaurantTable, table_id)
        if not table or not table.is_active:
            raise ValueError("Invalid or inactive table.")
        
        # Check if there is already an active order for this table
        existing_order = Order.query.filter_by(table_id=table_id, status='active').first()
        if existing_order:
            return existing_order

        active_orders_count = BillingService.get_orders_today_count(branch_id)
        # Generate order number (e.g., ORD-20231015-001)
        order_num = generate_order_number(active_orders_count)

        order = Order(
            order_id=order_num,
            table_id=table_id,
            status='active',
            created_by=user_id,
            branch_id=branch_id,
            created_at=get_ist_now()
        )
        table.status = 'occupied'
        
        db.session.add(order)
        db.session.commit()
        return order

    @staticmethod
    def update_order_item(order_id, menu_item_id, quantity, notes=None):
        order = db.session.get(Order, order_id)
        if not order or order.status != 'active':
            raise ValueError("Order is inactive or does not exist.")

        menu_item = db.session.get(MenuItem, menu_item_id)
        if not menu_item or not menu_item.is_available:
            raise ValueError("Menu item is unavailable.")

        order_item = OrderItem.query.filter_by(order_id=order_id, menu_item_id=menu_item_id).first()

        if quantity <= 0:
            if order_item:
                db.session.delete(order_item)
                db.session.commit()
            return None

        unit_price = Decimal(str(menu_item.price))
        total_price = unit_price * Decimal(str(quantity))

        if order_item:
            order_item.quantity = quantity
            order_item.total_price = total_price
            if notes is not None:
                order_item.notes = notes
        else:
            order_item = OrderItem(
                order_id=order_id,
                menu_item_id=menu_item_id,
                quantity=quantity,
                unit_price=unit_price,
                total_price=total_price,
                notes=notes
            )
            db.session.add(order_item)

        db.session.commit()
        return order_item

    @staticmethod
    def calculate_order_totals(order, discount_amount=0):
        subtotal = Decimal('0.00')
        tax_amount = Decimal('0.00')

        for item in order.items:
            item_total = Decimal(str(item.total_price))
            subtotal += item_total
            
            # Lookup GST rate based on menu item category
            rate = GST_RATES.get(item.menu_item.category, DEFAULT_GST_RATE)
            tax_amount += item_total * Decimal(str(rate))

        discount = Decimal(str(discount_amount))
        total_amount = subtotal + tax_amount - discount
        if total_amount < 0:
            total_amount = Decimal('0.00')

        return {
            'subtotal': subtotal.quantize(Decimal('0.01')),
            'tax_amount': tax_amount.quantize(Decimal('0.01')),
            'discount_amount': discount.quantize(Decimal('0.01')),
            'total_amount': total_amount.quantize(Decimal('0.01'))
        }

    @staticmethod
    def generate_bill(order_id, billed_by_user_id, payment_mode, discount_amount=0, notes=None):
        order = db.session.get(Order, order_id)
        if not order or order.status != 'active':
            raise ValueError("Cannot bill an inactive or non-existent order.")

        if not order.items:
            raise ValueError("Cannot generate a bill for an empty order.")

        totals = BillingService.calculate_order_totals(order, discount_amount)

        bills_count = BillingService.get_bills_today_count(order.branch_id)
        bill_num = generate_bill_number(bills_count)

        ist_now = get_ist_now()

        bill = Bill(
            bill_id=bill_num,
            order_id=order.order_id,
            table_id=order.table_id,
            subtotal=totals['subtotal'],
            tax_amount=totals['tax_amount'],
            discount_amount=totals['discount_amount'],
            total_amount=totals['total_amount'],
            payment_mode=payment_mode,
            payment_status='paid',  # Marked as fully paid upon checkout in this implementation
            billed_by=billed_by_user_id,
            branch_id=order.branch_id,
            bill_date=ist_now.date(),
            bill_time=ist_now.time(),
            created_at=ist_now,
            notes=notes
        )

        # Update order and table statuses
        order.status = 'completed'
        order.table.status = 'available'

        db.session.add(bill)
        db.session.commit()
        return bill
