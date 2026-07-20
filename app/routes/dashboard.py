import calendar
from flask import Blueprint, render_template, redirect, url_for, request
from flask_login import login_required, current_user
from app.extensions import db
from app.models.bill import Bill
from app.models.expense import Expense
from app.models.table import RestaurantTable
from app.models.branch import Branch
from app.services.report_service import ReportService
from app.utils.decorators import role_required
from app.utils.helpers import get_ist_date
from datetime import datetime

dashboard_bp = Blueprint('dashboard', __name__)

@dashboard_bp.route('/')
def index():
    if not current_user.is_authenticated:
        return redirect(url_for('auth.login'))
    
    if current_user.is_owner:
        return redirect(url_for('dashboard.owner_dashboard'))
    elif current_user.is_manager:
        return redirect(url_for('dashboard.manager_dashboard'))
    else:
        return redirect(url_for('dashboard.biller_dashboard'))


@dashboard_bp.route('/owner')
@login_required
@role_required('owner')
def owner_dashboard():
    today = get_ist_date()
    start_of_period = today
    end_of_period = today
    
    # Handle branch filter
    branch_id_filter = request.args.get('branch_id', type=int)
    if branch_id_filter == 0:
        branch_id_filter = None
        
    summary = ReportService.get_cashflow_summary(branch_id_filter, start_of_period, end_of_period)
    
    # Recent bills
    bill_query = Bill.query
    if branch_id_filter:
        bill_query = bill_query.filter_by(branch_id=branch_id_filter)
    recent_bills = bill_query.order_by(Bill.created_at.desc()).limit(10).all()
    
    # Recent expenses
    expense_query = Expense.query
    if branch_id_filter:
        expense_query = expense_query.filter_by(branch_id=branch_id_filter)
    recent_expenses = expense_query.order_by(Expense.created_at.desc()).limit(10).all()
    
    branches = Branch.query.filter_by(is_active=True).all()
    
    return render_template(
        'dashboard/owner_dashboard.html',
        summary=summary,
        recent_bills=recent_bills,
        recent_expenses=recent_expenses,
        branches=branches,
        selected_branch_id=branch_id_filter or 0,
        today_date=today
    )


@dashboard_bp.route('/manager')
@login_required
@role_required('owner', 'branch_manager')
def manager_dashboard():
    # If owner accesses it but has no branch, redirect to owner dashboard
    branch_id = current_user.branch_id
    if not branch_id:
        if current_user.is_owner:
            first_branch = Branch.query.filter_by(is_active=True).first()
            if first_branch:
                branch_id = first_branch.branch_id
            else:
                return redirect(url_for('dashboard.owner_dashboard'))
        else:
            return redirect(url_for('auth.login'))
            
    today = get_ist_date()
    start_of_period = today
    end_of_period = today
    
    summary = ReportService.get_cashflow_summary(branch_id, start_of_period, end_of_period)
    
    recent_bills = Bill.query.filter_by(branch_id=branch_id).order_by(Bill.created_at.desc()).limit(10).all()
    recent_expenses = Expense.query.filter_by(branch_id=branch_id).order_by(Expense.created_at.desc()).limit(10).all()
    
    branch = db.session.get(Branch, branch_id)
    
    return render_template(
        'dashboard/manager_dashboard.html',
        summary=summary,
        recent_bills=recent_bills,
        recent_expenses=recent_expenses,
        branch=branch,
        today_date=today
    )


@dashboard_bp.route('/biller')
@login_required
def biller_dashboard():
    # Billers or anyone else
    branch_id = current_user.branch_id
    if not branch_id:
        if current_user.is_owner:
            first_branch = Branch.query.filter_by(is_active=True).first()
            if first_branch:
                branch_id = first_branch.branch_id
            else:
                return "Please seed branches first.", 400
        else:
            return "Unauthorized. No branch assigned.", 403
            
    tables = RestaurantTable.query.filter_by(branch_id=branch_id, is_active=True).order_by(RestaurantTable.table_id).all()
    branch = db.session.get(Branch, branch_id)
    
    return render_template(
        'dashboard/biller_dashboard.html',
        tables=tables,
        branch=branch
    )
