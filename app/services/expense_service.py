from datetime import datetime, date
from decimal import Decimal
from sqlalchemy import extract, func
from app.extensions import db
from app.models.expense import Expense, ExpenseCategory
from app.models.grocery import GroceryItem, GroceryPurchase
from app.models.employee import SalaryPayment

class ExpenseService:
    @staticmethod
    def record_grocery_purchase(purchase_date, purchase_time, grocery_item_id, quantity, unit_price, vendor_name, notes, user_id, branch_id):
        qty = Decimal(str(quantity))
        price = Decimal(str(unit_price))
        total = qty * price

        purchase = GroceryPurchase(
            purchase_date=purchase_date,
            purchase_time=purchase_time,
            grocery_item_id=grocery_item_id,
            quantity=qty,
            unit_price=price,
            total_price=total,
            vendor_name=vendor_name,
            notes=notes,
            recorded_by=user_id,
            branch_id=branch_id,
            created_at=datetime.utcnow()
        )
        db.session.add(purchase)
        db.session.commit()
        return purchase

    @staticmethod
    def record_bulk_grocery_purchase(purchase_date, purchase_time, items_data, vendor_name, notes, user_id, branch_id):
        purchases = []
        for item in items_data:
            qty = Decimal(str(item['quantity']))
            total = Decimal(str(item['total_price']))
            price = total / qty if qty > 0 else Decimal('0')

            purchase = GroceryPurchase(
                purchase_date=purchase_date,
                purchase_time=purchase_time,
                grocery_item_id=item['grocery_item_id'],
                quantity=qty,
                unit_price=price,
                total_price=total,
                vendor_name=vendor_name,
                notes=notes,
                recorded_by=user_id,
                branch_id=branch_id,
                created_at=datetime.utcnow()
            )
            db.session.add(purchase)
            purchases.append(purchase)
            
        db.session.commit()
        return purchases

    @staticmethod
    def record_general_expense(expense_date, expense_time, category_id, description, amount, payment_mode, vendor_name, receipt_number, notes, user_id, branch_id):
        expense = Expense(
            expense_date=expense_date,
            expense_time=expense_time,
            category_id=category_id,
            description=description,
            amount=Decimal(str(amount)),
            payment_mode=payment_mode,
            vendor_name=vendor_name,
            receipt_number=receipt_number,
            notes=notes,
            recorded_by=user_id,
            branch_id=branch_id,
            created_at=datetime.utcnow()
        )
        db.session.add(expense)
        db.session.commit()
        return expense

    @staticmethod
    def get_monthly_outflow(branch_id, year, month):
        """
        Calculates and returns details of cash outflow for a branch in a given month:
        - Groceries
        - Other general expenses
        - Paid salary payments
        """
        # Grocery purchases
        groceries_sum = db.session.query(func.sum(GroceryPurchase.total_price)).filter(
            GroceryPurchase.branch_id == branch_id,
            extract('year', GroceryPurchase.purchase_date) == year,
            extract('month', GroceryPurchase.purchase_date) == month
        ).scalar() or Decimal('0.00')

        # Other expenses
        expenses_sum = db.session.query(func.sum(Expense.amount)).filter(
            Expense.branch_id == branch_id,
            extract('year', Expense.expense_date) == year,
            extract('month', Expense.expense_date) == month
        ).scalar() or Decimal('0.00')

        # Salaries paid
        salaries_sum = db.session.query(func.sum(SalaryPayment.net_salary)).filter(
            SalaryPayment.branch_id == branch_id,
            SalaryPayment.payment_year == year,
            SalaryPayment.payment_month == month,
            SalaryPayment.payment_status == 'paid'
        ).scalar() or Decimal('0.00')

        groceries_sum = Decimal(str(groceries_sum))
        expenses_sum = Decimal(str(expenses_sum))
        salaries_sum = Decimal(str(salaries_sum))
        total_outflow = groceries_sum + expenses_sum + salaries_sum

        return {
            'groceries': groceries_sum,
            'other_expenses': expenses_sum,
            'salaries': salaries_sum,
            'total_outflow': total_outflow
        }
