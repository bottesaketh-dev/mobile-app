from flask import Blueprint, render_template, redirect, url_for, flash, request, abort, send_file
from flask_login import login_required, current_user
from app.extensions import db
from app.models.table import RestaurantTable
from app.models.order import Order
from app.models.menu import MenuItem
from app.models.bill import Bill
from app.models.branch import Branch
from app.services.billing_service import BillingService
from app.forms.billing_forms import ProcessPaymentForm
from app.utils.bill_generator import generate_bill_pdf

billing_bp = Blueprint('billing', __name__)

@billing_bp.route('/')
@login_required
def table_view():
    branch_id = current_user.branch_id
    branch_filter = request.args.get('branch_id', type=int)
    
    if not branch_id:
        if current_user.is_owner:
            if branch_filter:
                branch_id = branch_filter
            else:
                first_branch = Branch.query.filter_by(is_active=True).first()
                if first_branch:
                    branch_id = first_branch.branch_id
                else:
                    return "Please configure a branch in management first.", 400
        else:
            return "Unauthorized. No branch assigned.", 403
            
    tables = RestaurantTable.query.filter_by(branch_id=branch_id, is_active=True).order_by(RestaurantTable.table_id).all()
    branches = Branch.query.filter_by(is_active=True).all() if current_user.is_owner else []
    
    return render_template('billing/table_view.html', tables=tables, branches=branches, selected_branch_id=branch_id)


@billing_bp.route('/pos/<string:table_id>')
@login_required
def pos_interface(table_id):
    table = RestaurantTable.query.get_or_404(table_id)
    branch_id = current_user.branch_id or table.branch_id
    
    # Restrict billers to their own branch
    if not current_user.is_owner and table.branch_id != current_user.branch_id:
        abort(403)
        
    # Get or create active order for this table
    order = Order.query.filter_by(table_id=table.table_id, status='active').first()
    if not order:
        order = BillingService.create_order(table.table_id, current_user.user_id, branch_id)
        
    # Fetch menu items available in this branch
    menu_items = MenuItem.query.filter_by(branch_id=branch_id, is_available=True).all()
    
    # Categorize items
    categorized_items = {}
    for item in menu_items:
        categorized_items.setdefault(item.category, []).append(item)
        
    totals = BillingService.calculate_order_totals(order)
    
    return render_template(
        'billing/pos_interface.html',
        table=table,
        order=order,
        categorized_items=categorized_items,
        totals=totals
    )


@billing_bp.route('/checkout/<string:order_id>', methods=['GET', 'POST'])
@login_required
def checkout(order_id):
    order = Order.query.get_or_404(order_id)
    if not current_user.is_owner and order.branch_id != current_user.branch_id:
        abort(403)
        
    if order.status != 'active':
        flash('Order has already been checked out or cancelled.', 'warning')
        return redirect(url_for('billing.table_view'))
        
    if not order.items:
        flash('Cannot checkout an empty order. Please add menu items.', 'danger')
        return redirect(url_for('billing.pos_interface', table_id=order.table_id))
        
    form = ProcessPaymentForm()
    totals = BillingService.calculate_order_totals(order)
    
    if form.validate_on_submit():
        discount = form.discount_amount.data or 0
        try:
            bill = BillingService.generate_bill(
                order_id=order.order_id,
                billed_by_user_id=current_user.user_id,
                payment_mode=form.payment_mode.data,
                discount_amount=discount,
                notes=form.notes.data
            )
            flash(f'Invoice {bill.bill_id} generated successfully!', 'success')
            return redirect(url_for('billing.receipt', bill_id=bill.bill_id))
        except ValueError as e:
            flash(str(e), 'danger')
            
    # Set default values or re-calculate dynamically on form change
    return render_template(
        'billing/payment.html',
        order=order,
        totals=totals,
        form=form
    )


@billing_bp.route('/receipt/<string:bill_id>')
@login_required
def receipt(bill_id):
    bill = Bill.query.get_or_404(bill_id)
    if not current_user.is_owner and bill.branch_id != current_user.branch_id:
        abort(403)
        
    return render_template('billing/generate_bill.html', bill=bill)


@billing_bp.route('/receipt/<string:bill_id>/pdf')
@login_required
def download_pdf(bill_id):
    bill = Bill.query.get_or_404(bill_id)
    if not current_user.is_owner and bill.branch_id != current_user.branch_id:
        abort(403)
        
    try:
        pdf_buffer = generate_bill_pdf(bill)
        return send_file(
            pdf_buffer,
            mimetype='application/pdf',
            as_attachment=True,
            download_name=f"{bill.bill_id}.pdf"
        )
    except Exception as e:
        flash(f"Error generating PDF: {str(e)}", 'danger')
        return redirect(url_for('billing.receipt', bill_id=bill.bill_id))
