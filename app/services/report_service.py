from datetime import datetime, date
from decimal import Decimal
from sqlalchemy import func, extract
from app.extensions import db
from app.models.bill import Bill
from app.models.grocery import GroceryPurchase, GroceryCategory, GroceryItem
from app.models.expense import Expense, ExpenseCategory
from app.models.employee import SalaryPayment

class ReportService:
    @staticmethod
    def get_cashflow_summary(branch_id, start_date, end_date):
        """
        Calculates total cash inflow (sales) and total outflows (groceries, expenses, salaries)
        for a given date range. If branch_id is None, aggregates for all branches (Owner view).
        """
        # Inflows (Sales)
        inflow_query = db.session.query(func.sum(Bill.total_amount))
        if branch_id:
            inflow_query = inflow_query.filter(Bill.branch_id == branch_id)
        inflow_query = inflow_query.filter(Bill.bill_date >= start_date, Bill.bill_date <= end_date)
        total_inflow = inflow_query.scalar() or Decimal('0.00')

        # Outflows: Groceries
        grocery_query = db.session.query(func.sum(GroceryPurchase.total_price))
        if branch_id:
            grocery_query = grocery_query.filter(GroceryPurchase.branch_id == branch_id)
        grocery_query = grocery_query.filter(GroceryPurchase.purchase_date >= start_date, GroceryPurchase.purchase_date <= end_date)
        grocery_outflow = grocery_query.scalar() or Decimal('0.00')

        # Outflows: General Expenses
        expense_query = db.session.query(func.sum(Expense.amount))
        if branch_id:
            expense_query = expense_query.filter(Expense.branch_id == branch_id)
        expense_query = expense_query.filter(Expense.expense_date >= start_date, Expense.expense_date <= end_date)
        expense_outflow = expense_query.scalar() or Decimal('0.00')

        # Outflows: Salaries (paid date falls in range)
        salary_query = db.session.query(func.sum(SalaryPayment.net_salary)).filter(SalaryPayment.payment_status == 'paid')
        if branch_id:
            salary_query = salary_query.filter(SalaryPayment.branch_id == branch_id)
        salary_query = salary_query.filter(SalaryPayment.payment_date >= start_date, SalaryPayment.payment_date <= end_date)
        salary_outflow = salary_query.scalar() or Decimal('0.00')

        total_inflow = Decimal(str(total_inflow))
        grocery_outflow = Decimal(str(grocery_outflow))
        expense_outflow = Decimal(str(expense_outflow))
        salary_outflow = Decimal(str(salary_outflow))

        total_outflow = grocery_outflow + expense_outflow + salary_outflow
        net_cashflow = total_inflow - total_outflow

        return {
            'inflow': total_inflow,
            'outflow_groceries': grocery_outflow,
            'outflow_expenses': expense_outflow,
            'outflow_salaries': salary_outflow,
            'total_outflow': total_outflow,
            'net_cashflow': net_cashflow
        }

    @staticmethod
    def get_dashboard_trends(branch_id, current_start, current_end, current_summary):
        from datetime import timedelta
        
        # Calculate duration of the current period
        delta = (current_end - current_start).days + 1
        
        prior_end = current_start - timedelta(days=1)
        prior_start = prior_end - timedelta(days=delta - 1)
        
        prior_summary = ReportService.get_cashflow_summary(branch_id, prior_start, prior_end)
        
        def calc_pct(curr, prior):
            # Convert to float for safe division
            curr_val = float(curr)
            prior_val = float(prior)
            if prior_val > 0:
                return round(((curr_val - prior_val) / prior_val) * 100, 1)
            elif curr_val > 0:
                return 100.0
            return 0.0

        return {
            'inflow_pct': calc_pct(current_summary['inflow'], prior_summary['inflow']),
            'outflow_pct': calc_pct(current_summary['total_outflow'], prior_summary['total_outflow']),
            'net_pct': calc_pct(current_summary['net_cashflow'], prior_summary['net_cashflow'])
        }


    @staticmethod
    def get_daily_sales_data(branch_id, start_date, end_date):
        """
        Retrieves daily revenue totals for a line chart.
        """
        sales_query = db.session.query(Bill.bill_date, func.sum(Bill.total_amount))
        if branch_id:
            sales_query = sales_query.filter(Bill.branch_id == branch_id)
        sales_data = sales_query.filter(
            Bill.bill_date >= start_date, 
            Bill.bill_date <= end_date
        ).group_by(Bill.bill_date).order_by(Bill.bill_date).all()

        return [{'date': d.strftime('%d-%m-%Y'), 'amount': float(a)} for d, a in sales_data]

    @staticmethod
    def get_expenses_by_category_data(branch_id, start_date, end_date):
        """
        Retrieves general expenses and grocery expenditures grouped by category for visual charts.
        """
        # General expenses category aggregation
        expense_query = db.session.query(ExpenseCategory.name, func.sum(Expense.amount)).select_from(ExpenseCategory).join(Expense)
        if branch_id:
            expense_query = expense_query.filter(Expense.branch_id == branch_id)
        expense_data = expense_query.filter(
            Expense.expense_date >= start_date, 
            Expense.expense_date <= end_date
        ).group_by(ExpenseCategory.name).all()

        # Grocery categories aggregation
        grocery_query = db.session.query(GroceryCategory.name, func.sum(GroceryPurchase.total_price)).select_from(GroceryCategory).join(GroceryItem).join(GroceryPurchase)
        if branch_id:
            grocery_query = grocery_query.filter(GroceryPurchase.branch_id == branch_id)
        grocery_data = grocery_query.filter(
            GroceryPurchase.purchase_date >= start_date, 
            GroceryPurchase.purchase_date <= end_date
        ).group_by(GroceryCategory.name).all()

        categories = []
        amounts = []

        # Merge them
        merged = {}
        for cat, amt in expense_data:
            merged[cat] = merged.get(cat, Decimal('0.00')) + Decimal(str(amt))
        for cat, amt in grocery_data:
            merged[f"Groceries - {cat}"] = merged.get(f"Groceries - {cat}", Decimal('0.00')) + Decimal(str(amt))

        return [{'category': c, 'amount': float(a)} for c, a in merged.items()]

    @staticmethod
    def get_sales_by_payment_mode_data(branch_id, start_date, end_date):
        """
        Retrieves sales totals grouped by payment mode for a doughnut chart.
        """
        query = db.session.query(Bill.payment_mode, func.sum(Bill.total_amount)).select_from(Bill)
        if branch_id:
            query = query.filter(Bill.branch_id == branch_id)
        data = query.filter(
            Bill.bill_date >= start_date,
            Bill.bill_date <= end_date
        ).group_by(Bill.payment_mode).all()

        return [{'mode': mode.upper() if mode else "UNKNOWN", 'amount': float(amt)} for mode, amt in data]

    @staticmethod
    def get_profit_loss_statement(branch_id, year, month):
        """
        Assembles a comprehensive Profit and Loss statement for a given month.
        """
        # Revenue: Completed Bills
        revenue_query = db.session.query(func.sum(Bill.total_amount)).filter(
            extract('year', Bill.bill_date) == year,
            extract('month', Bill.bill_date) == month
        )
        if branch_id:
            revenue_query = revenue_query.filter(Bill.branch_id == branch_id)
        revenue = revenue_query.scalar() or Decimal('0.00')

        # COGS: Grocery Purchases
        cogs_query = db.session.query(func.sum(GroceryPurchase.total_price)).filter(
            extract('year', GroceryPurchase.purchase_date) == year,
            extract('month', GroceryPurchase.purchase_date) == month
        )
        if branch_id:
            cogs_query = cogs_query.filter(GroceryPurchase.branch_id == branch_id)
        cogs = cogs_query.scalar() or Decimal('0.00')

        # OPEX: Other general expenses
        opex_expenses_query = db.session.query(func.sum(Expense.amount)).filter(
            extract('year', Expense.expense_date) == year,
            extract('month', Expense.expense_date) == month
        )
        if branch_id:
            opex_expenses_query = opex_expenses_query.filter(Expense.branch_id == branch_id)
        opex_expenses = opex_expenses_query.scalar() or Decimal('0.00')

        # OPEX: Salaries paid
        opex_salaries_query = db.session.query(func.sum(SalaryPayment.net_salary)).filter(
            SalaryPayment.payment_year == year,
            SalaryPayment.payment_month == month,
            SalaryPayment.payment_status == 'paid'
        )
        if branch_id:
            opex_salaries_query = opex_salaries_query.filter(SalaryPayment.branch_id == branch_id)
        opex_salaries = opex_salaries_query.scalar() or Decimal('0.00')

        revenue = Decimal(str(revenue))
        cogs = Decimal(str(cogs))
        opex_expenses = Decimal(str(opex_expenses))
        opex_salaries = Decimal(str(opex_salaries))

        opex_total = opex_expenses + opex_salaries
        gross_profit = revenue - cogs
        net_profit = gross_profit - opex_total

        return {
            'revenue': revenue,
            'cogs': cogs,
            'opex_expenses': opex_expenses,
            'opex_salaries': opex_salaries,
            'opex_total': opex_total,
            'gross_profit': gross_profit,
            'net_profit': net_profit
        }
