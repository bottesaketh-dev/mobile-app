from flask import Blueprint, render_template, redirect, url_for, flash, request, abort
from flask_login import login_user, logout_user, login_required, current_user
from app.extensions import db
from app.models.user import User
from app.models.branch import Branch
from app.forms.auth_forms import LoginForm, RegisterForm, ProfileForm
from app.utils.decorators import role_required

auth_bp = Blueprint('auth', __name__)

@auth_bp.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect_to_role_dashboard(current_user.role)
        
    form = LoginForm()
    if form.validate_on_submit():
        user = User.query.filter_by(username=form.username.data).first()
        if user and user.check_password(form.password.data):
            if not user.is_active:
                flash('Your account has been deactivated.', 'danger')
                return render_template('auth/login.html', form=form)
                
            login_user(user, remember=form.remember_me.data)
            flash(f'Welcome back, {user.username}!', 'success')
            
            next_page = request.args.get('next')
            if next_page:
                return redirect(next_page)
            return redirect_to_role_dashboard(user.role)
        else:
            flash('Invalid username or password.', 'danger')
            
    return render_template('auth/login.html', form=form)


@auth_bp.route('/logout')
@login_required
def logout():
    logout_user()
    flash('You have been logged out.', 'info')
    return redirect(url_for('auth.login'))


@auth_bp.route('/register', methods=['GET', 'POST'])
@login_required
@role_required('owner')
def register():
    form = RegisterForm()
    
    # Populate branches
    branches = Branch.query.filter_by(is_active=True).all()
    form.branch_id.choices = [(0, 'No Branch (Owner only)')] + [(b.branch_id, b.name) for b in branches]
    
    if form.validate_on_submit():
        branch_id = None if form.branch_id.data == 0 else form.branch_id.data
        user = User(
            username=form.username.data,
            email=form.email.data,
            role=form.role.data,
            branch_id=branch_id
        )
        user.set_password(form.password.data)
        db.session.add(user)
        db.session.commit()
        flash('Staff member successfully registered!', 'success')
        return redirect(url_for('dashboard.owner_dashboard'))
        
    return render_template('auth/register.html', form=form)


@auth_bp.route('/profile', methods=['GET', 'POST'])
@login_required
def profile():
    form = ProfileForm(current_user)
    if form.validate_on_submit():
        current_user.username = form.username.data
        current_user.email = form.email.data
        if form.password.data:
            current_user.set_password(form.password.data)
        db.session.commit()
        flash('Your profile has been updated.', 'success')
        return redirect(url_for('profile'))
        
    elif request.method == 'GET':
        form.username.data = current_user.username
        form.email.data = current_user.email
        
    return render_template('auth/profile.html', form=form)


def redirect_to_role_dashboard(role):
    if role == 'owner':
        return redirect(url_for('dashboard.owner_dashboard'))
    elif role == 'branch_manager':
        return redirect(url_for('dashboard.manager_dashboard'))
    elif role == 'biller':
        return redirect(url_for('dashboard.biller_dashboard'))
    return redirect(url_for('dashboard.biller_dashboard'))
