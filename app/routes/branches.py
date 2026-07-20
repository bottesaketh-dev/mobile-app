from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_required
from app.extensions import db
from app.models.branch import Branch
from app.forms.branch_forms import BranchForm
from app.utils.decorators import role_required

branch_bp = Blueprint('branch', __name__)

@branch_bp.route('/')
@login_required
@role_required('owner')
def branch_list():
    branches = Branch.query.order_by(Branch.name).all()
    return render_template('branches/branch_list.html', branches=branches)

@branch_bp.route('/add', methods=['GET', 'POST'])
@login_required
@role_required('owner')
def add_branch():
    form = BranchForm()
    
    if form.validate_on_submit():
        branch = Branch(
            name=form.name.data,
            address=form.address.data,
            phone=form.phone.data,
            is_active=form.is_active.data
        )
        db.session.add(branch)
        db.session.commit()
        flash('New branch added successfully!', 'success')
        return redirect(url_for('branch.branch_list'))
        
    return render_template('branches/branch_form.html', form=form, is_edit=False)

@branch_bp.route('/edit/<int:branch_id>', methods=['GET', 'POST'])
@login_required
@role_required('owner')
def edit_branch(branch_id):
    branch = Branch.query.get_or_404(branch_id)
    form = BranchForm(obj=branch)
    
    if form.validate_on_submit():
        form.populate_obj(branch)
        db.session.commit()
        flash('Branch updated successfully!', 'success')
        return redirect(url_for('branch.branch_list'))
        
    return render_template('branches/branch_form.html', form=form, is_edit=True, branch=branch)

@branch_bp.route('/toggle/<int:branch_id>', methods=['POST'])
@login_required
@role_required('owner')
def toggle_branch(branch_id):
    branch = Branch.query.get_or_404(branch_id)
    branch.is_active = not branch.is_active
    db.session.commit()
    
    status = "activated" if branch.is_active else "deactivated"
    flash(f"Branch '{branch.name}' has been {status}.", 'info')
    return redirect(url_for('branch.branch_list'))
