import csv
from datetime import datetime, date, timedelta
import calendar
from flask import Blueprint, render_template, request, make_response, flash, abort, redirect, url_for
from flask_login import login_required, current_user
from app.extensions import db
from app.models.bill import Bill
from app.models.expense import Expense
from app.models.grocery import GroceryPurchase, GroceryItem
from app.models.employee import SalaryPayment
from app.models.branch import Branch
from app.services.report_service import ReportService
from app.utils.decorators import role_required
from app.utils.helpers import get_ist_date

reports_bp = Blueprint('reports', __name__)

def parse_dates_or_defaults():
    today = get_ist_date()
    # Default to first day of current month to today
    default_start = today.replace(day=1)
    
    start_str = request.args.get('start_date', '')
    end_str = request.args.get('end_date', '')
    
    try:
        start_date = datetime.strptime(start_str, '%Y-%m-%d').date() if start_str else default_start
    except ValueError:
        start_date = default_start
        
    try:
        end_date = datetime.strptime(end_str, '%Y-%m-%d').date() if end_str else today
    except ValueError:
        end_date = today
        
    return start_date, end_date


@reports_bp.route('/cashflow')
@login_required
@role_required('owner', 'branch_manager')
def cashflow_overview():
    branch_id = current_user.branch_id
    branch_filter = request.args.get('branch_id', type=int)
    
    # Restrict branch manager to their own branch
    if not current_user.is_owner:
        branch_filter = branch_id
        
    start_date, end_date = parse_dates_or_defaults()
    summary = ReportService.get_cashflow_summary(branch_filter, start_date, end_date)
    
    branches = Branch.query.filter_by(is_active=True).all() if current_user.is_owner else []
    
    return render_template(
        'reports/cashflow_overview.html',
        summary=summary,
        start_date=start_date,
        end_date=end_date,
        branches=branches,
        selected_branch_id=branch_filter or 0
    )


@reports_bp.route('/daily')
@login_required
@role_required('owner', 'branch_manager')
def daily_report():
    branch_id = current_user.branch_id
    branch_filter = request.args.get('branch_id', type=int)
    
    if not current_user.is_owner:
        branch_filter = branch_id
        
    start_date, end_date = parse_dates_or_defaults()
    sales_data = ReportService.get_daily_sales_data(branch_filter, start_date, end_date)
    
    branches = Branch.query.filter_by(is_active=True).all() if current_user.is_owner else []
    
    return render_template(
        'reports/daily_report.html',
        sales_data=sales_data,
        start_date=start_date,
        end_date=end_date,
        branches=branches,
        selected_branch_id=branch_filter or 0
    )


@reports_bp.route('/monthly')
@login_required
@role_required('owner')  # Overall monthly details restricted to Owner
def monthly_report():
    today = get_ist_date()
    month = request.args.get('month', today.month, type=int)
    year = request.args.get('year', today.year, type=int)
    branch_filter = request.args.get('branch_id', type=int)
    
    statement = ReportService.get_profit_loss_statement(branch_filter, year, month)
    
    branches = Branch.query.filter_by(is_active=True).all()
    
    months = [(m, date(2000, m, 1).strftime('%B')) for m in range(1, 13)]
    years = list(range(2020, 2031))
    
    return render_template(
        'reports/monthly_report.html',
        statement=statement,
        selected_month=month,
        selected_year=year,
        months=months,
        years=years,
        branches=branches,
        selected_branch_id=branch_filter or 0,
        month_name=date(2000, month, 1).strftime('%B')
    )


@reports_bp.route('/grocery-trends')
@login_required
@role_required('owner', 'branch_manager')
def grocery_trends():
    branch_id = current_user.branch_id
    branch_filter = request.args.get('branch_id', type=int)
    
    if not current_user.is_owner:
        branch_filter = branch_id
        
    start_date, end_date = parse_dates_or_defaults()
    
    grocery_items = GroceryItem.query.filter_by(is_active=True).order_by(GroceryItem.product_name).all()
    selected_item_id = request.args.get('grocery_item_id')
    
    trend_data = []
    purchases = []
    selected_item = None
    
    if selected_item_id:
        selected_item = db.session.get(GroceryItem, selected_item_id)
        if selected_item:
            query = GroceryPurchase.query.filter(
                GroceryPurchase.grocery_item_id == selected_item_id,
                GroceryPurchase.purchase_date >= start_date,
                GroceryPurchase.purchase_date <= end_date
            )
            if branch_filter:
                query = query.filter_by(branch_id=branch_filter)
                
            purchases = query.order_by(GroceryPurchase.purchase_date, GroceryPurchase.purchase_time).all()
            
            from decimal import Decimal
            daily_prices = {}
            for p in purchases:
                d_str = p.purchase_date.strftime('%Y-%m-%d')
                if d_str not in daily_prices:
                    daily_prices[d_str] = {'total_price_sum': Decimal('0'), 'total_qty': Decimal('0')}
                daily_prices[d_str]['total_price_sum'] += p.total_price
                daily_prices[d_str]['total_qty'] += p.quantity
                
            for d_str in sorted(daily_prices.keys()):
                avg_price = daily_prices[d_str]['total_price_sum'] / daily_prices[d_str]['total_qty']
                trend_data.append({
                    'date': d_str,
                    'avg_price': round(float(avg_price), 2)
                })
                
    branches = Branch.query.filter_by(is_active=True).all() if current_user.is_owner else []
    
    return render_template(
        'reports/grocery_trends.html',
        grocery_items=grocery_items,
        selected_item_id=selected_item_id,
        selected_item=selected_item,
        trend_data=trend_data,
        purchases=purchases,
        start_date=start_date,
        end_date=end_date,
        branches=branches,
        selected_branch_id=branch_filter or 0
    )


@reports_bp.route('/export')
@login_required
@role_required('owner', 'branch_manager')
def export_csv():
    branch_id = current_user.branch_id
    branch_filter = request.args.get('branch_id', type=int)
    
    if not current_user.is_owner:
        branch_filter = branch_id
        
    start_date, end_date = parse_dates_or_defaults()
    
    # Retrieve all bills in date range
    bill_query = Bill.query.filter(Bill.bill_date >= start_date, Bill.bill_date <= end_date)
    if branch_filter:
        bill_query = bill_query.filter_by(branch_id=branch_filter)
    bills = bill_query.order_by(Bill.created_at).all()
    
    # Retrieve all expenses
    expense_query = Expense.query.filter(Expense.expense_date >= start_date, Expense.expense_date <= end_date)
    if branch_filter:
        expense_query = expense_query.filter_by(branch_id=branch_filter)
    expenses = expense_query.order_by(Expense.created_at).all()
    
    # Retrieve all grocery purchases
    grocery_query = GroceryPurchase.query.filter(GroceryPurchase.purchase_date >= start_date, GroceryPurchase.purchase_date <= end_date)
    if branch_filter:
        grocery_query = grocery_query.filter_by(branch_id=branch_filter)
    groceries = grocery_query.order_by(GroceryPurchase.created_at).all()

    # Create CSV in memory
    si = make_response()
    si.headers["Content-Disposition"] = f"attachment; filename=transaction_history_{start_date}_to_{end_date}.csv"
    si.headers["Content-Type"] = "text/csv"
    
    # Generate CSV lines
    def generate():
        data = csv.writer(si)
        # Header
        yield "DATE,TYPE,REFERENCE,DESCRIPTION,CATEGORY,PAYMENT_MODE,INFLOW (₹),OUTFLOW (₹),RECORDED_BY\n"
        
        # Add bills (Inflows)
        for b in bills:
            row = [
                b.bill_date.strftime('%Y-%m-%d'),
                "INFLOW",
                b.bill_id,
                f"Table {b.table.table_id} Bill",
                "Sales",
                b.payment_mode.upper(),
                f"{b.total_amount:.2f}",
                "0.00",
                b.biller.username
            ]
            yield ','.join(f'"{x}"' for x in row) + '\n'
            
        # Add general expenses (Outflows)
        for e in expenses:
            row = [
                e.expense_date.strftime('%Y-%m-%d'),
                "OUTFLOW",
                e.receipt_number or "N/A",
                e.description,
                f"Expense - {e.category.name}",
                e.payment_mode.upper(),
                "0.00",
                f"{e.amount:.2f}",
                e.recorder.username
            ]
            yield ','.join(f'"{x}"' for x in row) + '\n'
            
        # Add grocery purchases (Outflows)
        for g in groceries:
            row = [
                g.purchase_date.strftime('%Y-%m-%d'),
                "OUTFLOW",
                g.grocery_item.grocery_item_id,
                f"Grocery: {g.grocery_item.product_name} (Qty: {g.quantity} {g.grocery_item.unit})",
                f"Groceries - {g.grocery_item.category.name}",
                "CASH", # Default grocery purchases recorded as cash outflows
                "0.00",
                f"{g.total_price:.2f}",
                g.recorder.username
            ]
            yield ','.join(f'"{x}"' for x in row) + '\n'
            
    # Set the stream generators
    response = make_response(si)
    response.headers["Content-Disposition"] = f"attachment; filename=financials_{start_date}_to_{end_date}.csv"
    response.headers["Content-Type"] = "text/csv"
    
    csv_string = "".join(list(generate()))
    response.data = csv_string
    return response
