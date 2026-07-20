from flask import Blueprint, render_template, redirect, url_for, flash, request, abort
from flask_login import login_required, current_user
from app.extensions import db
from app.models.menu import MenuItem
from app.models.branch import Branch
from app.forms.menu_forms import MenuItemForm
from app.utils.decorators import role_required
from app.utils.constants import MENU_CATEGORIES

menu_bp = Blueprint('menu', __name__)

@menu_bp.route('/')
@login_required
def menu_list():
    # If the user belongs to a branch, restrict to branch items or let owners filter
    branch_id = current_user.branch_id
    
    # Query parameters
    search = request.args.get('search', '').strip()
    category = request.args.get('category', '').strip()
    branch_filter = request.args.get('branch_id', type=int)
    
    query = MenuItem.query
    
    # Apply filters
    if branch_id:
        query = query.filter_by(branch_id=branch_id)
    elif branch_filter:
        query = query.filter_by(branch_id=branch_filter)
        
    if search:
        query = query.filter(MenuItem.name.ilike(f"%{search}%"))
        
    if category and category in MENU_CATEGORIES:
        query = query.filter_by(category=category)
        
    # Paginate (20 items per page)
    page = request.args.get('page', 1, type=int)
    pagination = query.order_by(MenuItem.category, MenuItem.name).paginate(page=page, per_page=20)
    menu_items = pagination.items
    
    branches = Branch.query.filter_by(is_active=True).all() if current_user.is_owner else []
    
    return render_template(
        'menu/menu_list.html',
        menu_items=menu_items,
        pagination=pagination,
        categories=MENU_CATEGORIES,
        selected_category=category,
        search_query=search,
        branches=branches,
        selected_branch_id=branch_filter or 0
    )


@menu_bp.route('/add', methods=['GET', 'POST'])
@login_required
@role_required('owner', 'branch_manager')
def add_item():
    branches = Branch.query.filter_by(is_active=True).all() if current_user.is_owner else [db.session.get(Branch, current_user.branch_id)]
    categories = MENU_CATEGORIES
    
    if request.method == 'POST':
        names = request.form.getlist('name[]')
        descriptions = request.form.getlist('description[]')
        cats = request.form.getlist('category[]')
        prices = request.form.getlist('price[]')
        veg_flags = request.form.getlist('is_vegetarian[]')
        avail_flags = request.form.getlist('is_available[]')
        branch_ids = request.form.getlist('branch_id[]')
        
        if not names:
            flash("No items submitted.", "danger")
            return redirect(url_for('menu.add_item'))
            
        added_count = 0
        try:
            for i in range(len(names)):
                name = names[i].strip()
                if not name:
                    continue
                price = float(prices[i]) if prices[i] else 0.0
                branch_id = int(branch_ids[i])
                
                # Check permissions
                if not current_user.is_owner and branch_id != current_user.branch_id:
                    continue
                    
                item = MenuItem(
                    name=name,
                    description=descriptions[i].strip() if i < len(descriptions) else "",
                    category=cats[i] if i < len(cats) else categories[0],
                    price=price,
                    is_vegetarian=(veg_flags[i] == '1') if i < len(veg_flags) else False,
                    is_available=(avail_flags[i] == '1') if i < len(avail_flags) else True,
                    branch_id=branch_id
                )
                db.session.add(item)
                added_count += 1
                
            db.session.commit()
            if added_count > 0:
                flash(f'Successfully added {added_count} menu items!', 'success')
            else:
                flash('No valid items to add.', 'warning')
            return redirect(url_for('menu.menu_list'))
        except Exception as e:
            db.session.rollback()
            flash(f"Error adding items: {str(e)}", "danger")
            
    return render_template('menu/menu_add.html', branches=branches, categories=categories)


@menu_bp.route('/edit/<int:item_id>', methods=['GET', 'POST'])
@login_required
@role_required('owner', 'branch_manager')
def edit_item(item_id):
    item = MenuItem.query.get_or_404(item_id)
    
    # Check permissions (Managers cannot edit other branch items)
    if not current_user.is_owner and item.branch_id != current_user.branch_id:
        abort(403)
        
    form = MenuItemForm(obj=item)
    
    if current_user.is_owner:
        form.branch_id.choices = [(b.branch_id, b.name) for b in Branch.query.filter_by(is_active=True).all()]
    else:
        branch = db.session.get(Branch, current_user.branch_id)
        form.branch_id.choices = [(branch.branch_id, branch.name)]
        
    if form.validate_on_submit():
        form.populate_obj(item)
        db.session.commit()
        flash('Menu item successfully updated!', 'success')
        return redirect(url_for('menu.menu_list'))
        
    return render_template('menu/menu_edit.html', form=form, item=item)


@menu_bp.route('/toggle/<int:item_id>', methods=['POST'])
@login_required
@role_required('owner', 'branch_manager')
def toggle_availability(item_id):
    item = MenuItem.query.get_or_404(item_id)
    if not current_user.is_owner and item.branch_id != current_user.branch_id:
        abort(403)
        
    item.is_available = not item.is_available
    db.session.commit()
    status = "available" if item.is_available else "unavailable"
    flash(f"'{item.name}' is now marked as {status}.", 'info')
    return redirect(request.referrer or url_for('menu.menu_list'))
