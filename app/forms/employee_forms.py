from datetime import date
from flask_wtf import FlaskForm
from wtforms import StringField, DecimalField, SelectField, DateField, SubmitField
from wtforms.validators import DataRequired, Email, NumberRange, Length, Optional
from app.utils.constants import SALARY_PAYMENT_MODES
from app.utils.helpers import get_ist_date

class EmployeeForm(FlaskForm):
    first_name = StringField('First Name', validators=[DataRequired(), Length(max=50)])
    last_name = StringField('Last Name', validators=[DataRequired(), Length(max=50)])
    email = StringField('Email', validators=[Optional(), Email(), Length(max=120)])
    phone = StringField('Phone Number', validators=[DataRequired(), Length(min=10, max=15)])
    position = StringField('Position/Job Title', validators=[DataRequired(), Length(max=50)])
    monthly_salary = DecimalField('Monthly Salary (₹)', validators=[DataRequired(), NumberRange(min=0.00, message="Salary must be non-negative.")])
    join_date = DateField('Join Date', default=get_ist_date, validators=[DataRequired()])
    branch_id = SelectField('Branch', coerce=int, validators=[DataRequired()])
    submit = SubmitField('Save Employee')


class SalaryPaymentForm(FlaskForm):
    payment_month = SelectField('Month', coerce=int, choices=[(m, date(2000, m, 1).strftime('%B')) for m in range(1, 13)], validators=[DataRequired()])
    payment_year = SelectField('Year', coerce=int, choices=[(y, str(y)) for y in range(2020, 2031)], validators=[DataRequired()])
    base_salary = DecimalField('Base Salary (₹)', validators=[DataRequired(), NumberRange(min=0.00)])
    bonus = DecimalField('Bonus (₹)', default=0.00, validators=[NumberRange(min=0.00)])
    deductions = DecimalField('Deductions (₹)', default=0.00, validators=[NumberRange(min=0.00)])
    payment_status = SelectField('Payment Status', choices=[('pending', 'Pending'), ('paid', 'Paid')], validators=[DataRequired()])
    payment_mode = SelectField('Payment Mode', choices=[('', '---')] + [(mode, mode.upper()) for mode in SALARY_PAYMENT_MODES], validators=[Optional()])
    submit = SubmitField('Process Payroll')
