from datetime import date, time
from flask import Blueprint, render_template, redirect, url_for, flash, request, abort
from flask_login import login_required, current_user
from app.extensions import db
from app.models.expense import Expense, ExpenseCategory
from app.models.grocery import GroceryCategory, GroceryItem, GroceryPurchase
from app.models.branch import Branch
from app.forms.expense_forms import ExpenseForm, ExpenseCategoryForm
from app.forms.grocery_forms import GroceryItemForm, GroceryPurchaseForm
from app.services.expense_service import ExpenseService
from app.utils.decorators import role_required
from app.utils.helpers import generate_grocery_product_id, get_ist_now

expenses_bp = Blueprint('expenses', __name__)

@expenses_bp.route('/')
@login_required
@role_required('owner', 'branch_manager')
def expense_list():
    branch_id = current_user.branch_id
    branch_filter = request.args.get('branch_id', type=int)
    
    query = Expense.query
    if branch_id:
        query = query.filter_by(branch_id=branch_id)
    elif branch_filter:
        query = query.filter_by(branch_id=branch_filter)
        
    page = request.args.get('page', 1, type=int)
    pagination = query.order_by(Expense.expense_date.desc(), Expense.expense_time.desc()).paginate(page=page, per_page=20)
    expenses = pagination.items
    
    branches = Branch.query.filter_by(is_active=True).all() if current_user.is_owner else []
    
    return render_template(
        'expenses/expense_list.html',
        expenses=expenses,
        pagination=pagination,
        branches=branches,
        selected_branch_id=branch_filter or 0
    )


@expenses_bp.route('/add', methods=['GET', 'POST'])
@login_required
@role_required('owner', 'branch_manager')
def add_expense():
    categories = ExpenseCategory.query.filter_by(is_active=True).all()
    if current_user.is_owner:
        branches = Branch.query.filter_by(is_active=True).all()
    else:
        branches = [db.session.get(Branch, current_user.branch_id)]
        
    if request.method == 'POST':
        dates = request.form.getlist('expense_date[]')
        times = request.form.getlist('expense_time[]')
        cat_ids = request.form.getlist('category_id[]')
        descriptions = request.form.getlist('description[]')
        amounts = request.form.getlist('amount[]')
        payment_modes = request.form.getlist('payment_mode[]')
        vendors = request.form.getlist('vendor_name[]')
        receipts = request.form.getlist('receipt_number[]')
        branch_ids = request.form.getlist('branch_id[]')
        
        if not amounts:
            flash("No expenses submitted.", "danger")
            return redirect(url_for('expenses.add_expense'))
            
        added_count = 0
        try:
            for i in range(len(amounts)):
                amount_str = amounts[i].strip()
                if not amount_str:
                    continue
                amount = float(amount_str)
                if amount <= 0:
                    continue
                    
                e_date = dates[i] if i < len(dates) and dates[i] else str(get_ist_date())
                try:
                    e_date_obj = date.fromisoformat(e_date)
                except ValueError:
                    e_date_obj = get_ist_date()
                    
                e_time = times[i] if i < len(times) and times[i] else str(get_ist_time().replace(microsecond=0))
                try:
                    e_time_obj = time.fromisoformat(e_time)
                except ValueError:
                    e_time_obj = get_ist_time()
                    
                cat_id = int(cat_ids[i])
                desc = descriptions[i].strip() if i < len(descriptions) else ""
                p_mode = payment_modes[i].strip() if i < len(payment_modes) else ""
                vendor = vendors[i].strip() if i < len(vendors) else ""
                receipt = receipts[i].strip() if i < len(receipts) else ""
                branch_id = int(branch_ids[i]) if i < len(branch_ids) else current_user.branch_id
                
                ExpenseService.record_general_expense(
                    expense_date=e_date_obj,
                    expense_time=e_time_obj,
                    category_id=cat_id,
                    description=desc,
                    amount=amount,
                    payment_mode=p_mode,
                    vendor_name=vendor,
                    receipt_number=receipt,
                    notes="",
                    user_id=current_user.user_id,
                    branch_id=branch_id
                )
                added_count += 1
                
            if added_count > 0:
                flash(f'Successfully logged {added_count} general expenses!', 'success')
            else:
                flash('No valid expenses to log.', 'warning')
            return redirect(url_for('expenses.expense_list'))
        except Exception as e:
            flash(f"Error logging expenses: {str(e)}", "danger")
            
    from app.utils.constants import EXPENSE_PAYMENT_MODES
    return render_template(
        'expenses/add_expense.html', 
        categories=categories, 
        branches=branches, 
        default_date=get_ist_now().date().isoformat(), 
        default_time=get_ist_now().time().strftime('%H:%M'), 
        payment_modes=EXPENSE_PAYMENT_MODES
    )


@expenses_bp.route('/categories', methods=['GET', 'POST'])
@login_required
@role_required('owner', 'branch_manager')
def expense_categories():
    form = ExpenseCategoryForm()
    if form.validate_on_submit():
        # Check duplicate
        existing = ExpenseCategory.query.filter_by(name=form.name.data).first()
        if existing:
            flash('Category already exists.', 'danger')
        else:
            cat = ExpenseCategory(name=form.name.data, description=form.description.data)
            db.session.add(cat)
            db.session.commit()
            flash('Expense category added successfully!', 'success')
            return redirect(url_for('expenses.expense_categories'))
            
    categories = ExpenseCategory.query.order_by(ExpenseCategory.name).all()
    return render_template('expenses/expense_categories.html', categories=categories, form=form)


@expenses_bp.route('/groceries')
@login_required
@role_required('owner', 'branch_manager')
def grocery_list():
    branch_id = current_user.branch_id
    branch_filter = request.args.get('branch_id', type=int)
    
    query = GroceryPurchase.query
    if branch_id:
        query = query.filter_by(branch_id=branch_id)
    elif branch_filter:
        query = query.filter_by(branch_id=branch_filter)
        
    page = request.args.get('page', 1, type=int)
    pagination = query.order_by(GroceryPurchase.purchase_date.desc(), GroceryPurchase.purchase_time.desc()).paginate(page=page, per_page=20)
    purchases = pagination.items
    
    branches = Branch.query.filter_by(is_active=True).all() if current_user.is_owner else []
    
    return render_template(
        'expenses/grocery_list.html',
        purchases=purchases,
        pagination=pagination,
        branches=branches,
        selected_branch_id=branch_filter or 0
    )


@expenses_bp.route('/groceries/bulk-purchase', methods=['GET', 'POST'])
@login_required
@role_required('owner', 'branch_manager')
def bulk_grocery_purchase():
    if request.method == 'POST':
        purchase_date = request.form.get('purchase_date')
        purchase_time = request.form.get('purchase_time')
        vendor_name = request.form.get('vendor_name')
        notes = request.form.get('notes')
        branch_id = request.form.get('branch_id', type=int)
        
        if not branch_id and not current_user.is_owner:
            branch_id = current_user.branch_id
            
        items_data = []
        for key, value in request.form.items():
            if key.startswith('qty_') and value:
                item_id = key.replace('qty_', '')
                try:
                    qty = float(value)
                    if qty > 0:
                        total_price = float(request.form.get(f'total_{item_id}', 0))
                        items_data.append({
                            'grocery_item_id': item_id,
                            'quantity': qty,
                            'total_price': total_price
                        })
                except ValueError:
                    pass
                    
        if items_data:
            ExpenseService.record_bulk_grocery_purchase(
                purchase_date=purchase_date,
                purchase_time=purchase_time,
                items_data=items_data,
                vendor_name=vendor_name,
                notes=notes,
                user_id=current_user.user_id,
                branch_id=branch_id
            )
            flash(f'Successfully logged {len(items_data)} grocery items!', 'success')
            return redirect(url_for('expenses.grocery_list'))
        else:
            flash('No valid items with quantity > 0 were submitted.', 'warning')
            
    # GET request data
    categories = GroceryCategory.query.filter_by(is_active=True).all()
    branches = Branch.query.filter_by(is_active=True).all() if current_user.is_owner else []
    
    from app.utils.helpers import get_ist_date, get_ist_time
    today_date = get_ist_date()
    today_time = get_ist_time()
    
    return render_template(
        'expenses/bulk_grocery.html',
        categories=categories,
        branches=branches,
        today_date=today_date,
        today_time=today_time
    )

@expenses_bp.route('/groceries/add-purchase', methods=['GET', 'POST'])
@login_required
@role_required('owner', 'branch_manager')
def add_grocery_purchase():
    form = GroceryPurchaseForm()
    
    # Populate items
    items = GroceryItem.query.filter_by(is_active=True).order_by(GroceryItem.product_name).all()
    form.grocery_item_id.choices = [(i.grocery_item_id, f"{i.product_name} ({i.grocery_item_id})") for i in items]
    
    # Populate branch choices
    if current_user.is_owner:
        form.branch_id.choices = [(b.branch_id, b.name) for b in Branch.query.filter_by(is_active=True).all()]
    else:
        branch = db.session.get(Branch, current_user.branch_id)
        form.branch_id.choices = [(branch.branch_id, branch.name)]
        form.branch_id.data = branch.branch_id
        
    if form.validate_on_submit():
        branch_id = form.branch_id.data
            
        ExpenseService.record_grocery_purchase(
            purchase_date=form.purchase_date.data,
            purchase_time=form.purchase_time.data,
            grocery_item_id=form.grocery_item_id.data,
            quantity=form.quantity.data,
            unit_price=form.unit_price.data,
            vendor_name=form.vendor_name.data,
            notes=form.notes.data,
            user_id=current_user.user_id,
            branch_id=branch_id
        )
        flash('Grocery purchase logged successfully!', 'success')
        return redirect(url_for('expenses.grocery_list'))
        
    return render_template('expenses/add_grocery.html', form=form)


@expenses_bp.route('/groceries/add-item', methods=['GET', 'POST'])
@login_required
@role_required('owner', 'branch_manager')
def add_grocery_item():
    form = GroceryItemForm()
    
    # Populate categories
    categories = GroceryCategory.query.filter_by(is_active=True).all()
    form.category_id.choices = [(c.grocery_category_id, c.name) for c in categories]
    
    if form.validate_on_submit():
        # Check duplicate name
        existing = GroceryItem.query.filter_by(product_name=form.product_name.data).first()
        if existing:
            flash('Grocery product already exists.', 'danger')
        else:
            cat = db.session.get(GroceryCategory, form.category_id.data)
            items_count = GroceryItem.query.filter_by(category_id=cat.grocery_category_id).count()
            pid = generate_grocery_product_id(cat.name, items_count)
            
            item = GroceryItem(
                grocery_item_id=pid,
                product_name=form.product_name.data,
                category_id=cat.grocery_category_id,
                unit=form.unit.data
            )
            db.session.add(item)
            db.session.commit()
            flash('Grocery item stock profile created!', 'success')
            return redirect(url_for('expenses.add_grocery_purchase'))
            
    return render_template('expenses/add_grocery_item.html', form=form)
