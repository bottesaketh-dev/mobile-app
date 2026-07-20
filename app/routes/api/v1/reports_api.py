from datetime import datetime, date, timedelta
from flask import Blueprint, jsonify, request
from flask_login import login_required, current_user
from app.services.report_service import ReportService
from app.utils.decorators import role_required
from app.utils.helpers import get_ist_date

api_reports_bp = Blueprint('api_reports', __name__)

@api_reports_bp.route('/dashboard-charts', methods=['GET'])
@login_required
@role_required('owner', 'branch_manager')
def get_chart_data():
    """
    Returns cashflow statistics for the current month or filtered period.
    Used to populate line graphs and pie charts via AJAX.
    """
    branch_id = current_user.branch_id
    branch_filter = request.args.get('branch_id', type=int)
    
    if not current_user.is_owner:
        branch_filter = branch_id
    elif branch_filter == 0:
        branch_filter = None
        
    today = get_ist_date()
    # Default: last 30 days
    start_str = request.args.get('start_date', '')
    end_str = request.args.get('end_date', '')
    
    try:
        start_date = datetime.strptime(start_str, '%Y-%m-%d').date() if start_str else today - timedelta(days=30)
    except ValueError:
        start_date = today - timedelta(days=30)
        
    try:
        end_date = datetime.strptime(end_str, '%Y-%m-%d').date() if end_str else today
    except ValueError:
        end_date = today
        
    summary = ReportService.get_cashflow_summary(branch_filter, start_date, end_date)
    daily_sales = ReportService.get_daily_sales_data(branch_filter, start_date, end_date)
    expenses_categories = ReportService.get_expenses_by_category_data(branch_filter, start_date, end_date)
    payment_modes = ReportService.get_sales_by_payment_mode_data(branch_filter, start_date, end_date)

    return jsonify({
        'summary': {
            'inflow': float(summary['inflow']),
            'outflow_groceries': float(summary['outflow_groceries']),
            'outflow_expenses': float(summary['outflow_expenses']),
            'outflow_salaries': float(summary['outflow_salaries']),
            'total_outflow': float(summary['total_outflow']),
            'net_cashflow': float(summary['net_cashflow'])
        },
        'daily_sales': daily_sales,
        'expense_categories': expenses_categories,
        'payment_modes': payment_modes
    })
