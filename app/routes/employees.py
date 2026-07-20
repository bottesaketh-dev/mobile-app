from datetime import date
from flask import Blueprint, render_template, redirect, url_for, flash, request, abort
from flask_login import login_required, current_user
from app.extensions import db
from app.models.employee import Employee, SalaryPayment
from app.models.branch import Branch
from app.forms.employee_forms import EmployeeForm, SalaryPaymentForm
from app.utils.decorators import role_required
from app.utils.helpers import generate_employee_id, get_ist_date

employees_bp = Blueprint('employees', __name__)

@employees_bp.route('/')
@login_required
@role_required('owner')
def employee_list():
    branch_filter = request.args.get('branch_id', type=int)
    
    query = Employee.query
    if branch_filter:
        query = query.filter_by(branch_id=branch_filter)
        
    page = request.args.get('page', 1, type=int)
    pagination = query.order_by(Employee.employee_id).paginate(page=page, per_page=20)
    employees = pagination.items
    
    branches = Branch.query.filter_by(is_active=True).all()
    
    return render_template(
        'employees/employee_list.html',
        employees=employees,
        pagination=pagination,
        branches=branches,
        selected_branch_id=branch_filter or 0
    )


@employees_bp.route('/add', methods=['GET', 'POST'])
@login_required
@role_required('owner')
def add_employee():
    branches = Branch.query.filter_by(is_active=True).all()
    
    if request.method == 'POST':
        first_names = request.form.getlist('first_name[]')
        last_names = request.form.getlist('last_name[]')
        emails = request.form.getlist('email[]')
        phones = request.form.getlist('phone[]')
        positions = request.form.getlist('position[]')
        salaries = request.form.getlist('monthly_salary[]')
        join_dates = request.form.getlist('join_date[]')
        branch_ids = request.form.getlist('branch_id[]')
        
        if not first_names:
            flash("No employees submitted.", "danger")
            return redirect(url_for('employees.add_employee'))
            
        added_count = 0
        total_emp = Employee.query.count()
        
        try:
            for i in range(len(first_names)):
                fname = first_names[i].strip()
                lname = last_names[i].strip() if i < len(last_names) else ""
                if not fname:
                    continue
                    
                emp_id = generate_employee_id(total_emp + added_count)
                
                email = emails[i].strip() if i < len(emails) and emails[i].strip() else None
                phone = phones[i].strip() if i < len(phones) else ""
                position = positions[i].strip() if i < len(positions) else ""
                salary = float(salaries[i]) if i < len(salaries) and salaries[i] else 0.0
                j_date = join_dates[i] if i < len(join_dates) and join_dates[i] else str(get_ist_date())
                
                try:
                    j_date_obj = date.fromisoformat(j_date)
                except ValueError:
                    j_date_obj = get_ist_date()
                    
                branch_id = int(branch_ids[i])
                
                employee = Employee(
                    employee_id=emp_id,
                    first_name=fname,
                    last_name=lname,
                    email=email,
                    phone=phone,
                    position=position,
                    monthly_salary=salary,
                    join_date=j_date_obj,
                    branch_id=branch_id
                )
                db.session.add(employee)
                added_count += 1
                
            db.session.commit()
            if added_count > 0:
                flash(f'Successfully onboarded {added_count} employees!', 'success')
            else:
                flash('No valid employees to add.', 'warning')
            return redirect(url_for('employees.employee_list'))
        except Exception as e:
            db.session.rollback()
            flash(f"Error adding employees: {str(e)}", "danger")
            
    return render_template('employees/add_employee.html', branches=branches, default_date=get_ist_date().isoformat())


@employees_bp.route('/edit/<string:employee_id>', methods=['GET', 'POST'])
@login_required
@role_required('owner')
def edit_employee(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    form = EmployeeForm(obj=employee)
    
    branches = Branch.query.filter_by(is_active=True).all()
    form.branch_id.choices = [(b.branch_id, b.name) for b in branches]
    
    if form.validate_on_submit():
        form.populate_obj(employee)
        db.session.commit()
        flash('Employee records updated successfully.', 'success')
        return redirect(url_for('employees.employee_list'))
        
    return render_template('employees/edit_employee.html', form=form, employee=employee)


@employees_bp.route('/toggle/<string:employee_id>', methods=['POST'])
@login_required
@role_required('owner')
def toggle_status(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    employee.is_active = not employee.is_active
    db.session.commit()
    status = "activated" if employee.is_active else "deactivated"
    flash(f"Staff member {employee.full_name} has been {status}.", 'info')
    return redirect(url_for('employees.employee_list'))


@employees_bp.route('/salaries', methods=['GET'])
@login_required
@role_required('owner')
def salary_management():
    today = get_ist_date()
    # Read query params
    month = request.args.get('month', today.month, type=int)
    year = request.args.get('year', today.year, type=int)
    branch_filter = request.args.get('branch_id', type=int)
    
    query = Employee.query.filter_by(is_active=True)
    if branch_filter:
        query = query.filter_by(branch_id=branch_filter)
        
    employees = query.order_by(Employee.employee_id).all()
    
    # Check payment statuses for each employee in selected month/year
    payroll_data = []
    for emp in employees:
        payment = SalaryPayment.query.filter_by(
            employee_id=emp.employee_id,
            payment_month=month,
            payment_year=year
        ).first()
        
        payroll_data.append({
            'employee': emp,
            'payment': payment
        })
        
    branches = Branch.query.filter_by(is_active=True).all()
    
    months = [(m, date(2000, m, 1).strftime('%B')) for m in range(1, 13)]
    years = list(range(2020, 2031))
    
    return render_template(
        'employees/salary_management.html',
        payroll_data=payroll_data,
        selected_month=month,
        selected_year=year,
        months=months,
        years=years,
        branches=branches,
        selected_branch_id=branch_filter or 0
    )


@employees_bp.route('/salaries/pay/<string:employee_id>', methods=['GET', 'POST'])
@login_required
@role_required('owner')
def process_salary(employee_id):
    employee = Employee.query.get_or_404(employee_id)
    
    month = request.args.get('month', type=int)
    year = request.args.get('year', type=int)
    if not month or not year:
        flash("Invalid payroll period.", "danger")
        return redirect(url_for('employees.salary_management'))
        
    # Check if payment already exists
    existing = SalaryPayment.query.filter_by(
        employee_id=employee.employee_id,
        payment_month=month,
        payment_year=year
    ).first()
    
    if existing and existing.payment_status == 'paid':
        flash("Salary has already been processed and paid for this period.", "warning")
        return redirect(url_for('employees.salary_management', month=month, year=year))
        
    form = SalaryPaymentForm()
    
    if request.method == 'GET':
        form.payment_month.data = month
        form.payment_year.data = year
        form.base_salary.data = employee.monthly_salary
        
    if form.validate_on_submit():
        base = form.base_salary.data
        bonus = form.bonus.data or 0
        deductions = form.deductions.data or 0
        net = base + bonus - deductions
        if net < 0:
            net = 0
            
        p_date = get_ist_date() if form.payment_status.data == 'paid' else None
        p_mode = form.payment_mode.data if form.payment_status.data == 'paid' else None
        
        if existing:
            # Update pending payment
            existing.base_salary = base
            existing.bonus = bonus
            existing.deductions = deductions
            existing.net_salary = net
            existing.payment_status = form.payment_status.data
            existing.payment_date = p_date
            existing.payment_mode = p_mode
            existing.processed_by = current_user.user_id
        else:
            # Create new payment
            pay = SalaryPayment(
                employee_id=employee.employee_id,
                payment_month=month,
                payment_year=year,
                base_salary=base,
                bonus=bonus,
                deductions=deductions,
                net_salary=net,
                payment_date=p_date,
                payment_status=form.payment_status.data,
                payment_mode=p_mode,
                processed_by=current_user.user_id,
                branch_id=employee.branch_id
            )
            db.session.add(pay)
            
        db.session.commit()
        flash(f"Salary processed successfully for {employee.full_name}!", "success")
        return redirect(url_for('employees.salary_management', month=month, year=year))
        
    return render_template(
        'employees/salary_payment.html',
        employee=employee,
        form=form,
        month_name=date(2000, month, 1).strftime('%B'),
        year=year
    )
