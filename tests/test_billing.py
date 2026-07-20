import pytest
from decimal import Decimal
from app.models.table import RestaurantTable
from app.models.menu import MenuItem
from app.models.order import Order
from app.models.bill import Bill
from app.services.billing_service import BillingService

def test_order_and_billing_lifecycle(app, init_database):
    """
    Tests the complete lifecycle of dining room operations:
    1. Table allocation & order initiation.
    2. Adding menu items with calculations of unit costs.
    3. Applying a discount, calculating GST tax, and processing checkout.
    4. Table availability restoration.
    """
    with app.app_context():
        table = RestaurantTable.query.filter_by(table_id="T1").first()
        dish = MenuItem.query.filter_by(name="Paneer Tikka").first()
        branch_id = table.branch_id
        
        # Verify table starts available
        assert table.status == "available"
        
        # 1. Create order
        order = BillingService.create_order(
            table_id=table.id,
            user_id=3,  # Biller user ID seeded
            branch_id=branch_id
        )
        assert order.status == "active"
        assert table.status == "occupied"
        
        # 2. Add menu item
        BillingService.update_order_item(
            order_id=order.id,
            menu_item_id=dish.id,
            quantity=2,
            notes="Mild spice"
        )
        
        assert len(order.items) == 1
        assert order.items[0].quantity == 2
        assert order.items[0].total_price == Decimal("500.00")
        
        # 3. Calculate totals (Subtotal 500, GST on Starters is 5% -> 25, total 525)
        totals = BillingService.calculate_order_totals(order, discount_amount=50.00)
        assert totals['subtotal'] == Decimal("500.00")
        assert totals['tax_amount'] == Decimal("25.00")
        assert totals['discount_amount'] == Decimal("50.00")
        assert totals['total_amount'] == Decimal("475.00")  # 500 + 25 - 50 = 475
        
        # 4. Settle / Generate Bill
        bill = BillingService.generate_bill(
            order_id=order.id,
            billed_by_user_id=3,
            payment_mode="cash",
            discount_amount=50.00,
            notes="Cash paid"
        )
        
        assert bill.bill_id.startswith("INV-")
        assert bill.total_amount == Decimal("475.00")
        assert order.status == "completed"
        assert table.status == "available"  # Table restored to available


def test_report_chart_data(app, init_database):
    """
    Verifies that ReportService aggregation query executes without join errors.
    """
    from datetime import date
    from app.services.report_service import ReportService
    with app.app_context():
        result = ReportService.get_expenses_by_category_data(
            branch_id=None,
            start_date=date(2026, 1, 1),
            end_date=date(2026, 12, 31)
        )
        assert isinstance(result, list)

