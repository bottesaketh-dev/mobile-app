from datetime import date
from flask_wtf import FlaskForm
from wtforms import StringField, TextAreaField, DecimalField, SelectField, DateField, TimeField, SubmitField
from wtforms.validators import DataRequired, NumberRange, Length
from app.utils.constants import EXPENSE_PAYMENT_MODES
from app.utils.helpers import get_ist_date, get_ist_time

class ExpenseForm(FlaskForm):
    expense_date = DateField('Expense Date', default=get_ist_date, validators=[DataRequired()])
    expense_time = TimeField('Expense Time', default=get_ist_time, validators=[DataRequired()])
    category_id = SelectField('Expense Category', coerce=int, validators=[DataRequired()])
    description = StringField('Description', validators=[DataRequired(), Length(max=255)])
    amount = DecimalField('Amount (₹)', validators=[DataRequired(), NumberRange(min=0.01, message="Amount must be greater than zero.")])
    payment_mode = SelectField('Payment Mode', choices=[(mode, mode.upper()) for mode in EXPENSE_PAYMENT_MODES], validators=[DataRequired()])
    vendor_name = StringField('Vendor/Payee Name', validators=[Length(max=100)])
    receipt_number = StringField('Receipt/Voucher Number', validators=[Length(max=50)])
    notes = TextAreaField('Additional Notes')
    branch_id = SelectField('Branch', coerce=int)
    submit = SubmitField('Log Expense')


class ExpenseCategoryForm(FlaskForm):
    name = StringField('Category Name', validators=[DataRequired(), Length(max=50)])
    description = TextAreaField('Description', validators=[Length(max=200)])
    submit = SubmitField('Add Category')
