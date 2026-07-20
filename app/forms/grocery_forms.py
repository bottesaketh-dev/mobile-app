from datetime import date
from flask_wtf import FlaskForm
from wtforms import StringField, TextAreaField, DecimalField, SelectField, DateField, TimeField, SubmitField
from wtforms.validators import DataRequired, NumberRange, Length
from app.utils.constants import UNITS
from app.utils.helpers import get_ist_date, get_ist_time

class GroceryItemForm(FlaskForm):
    product_name = StringField('Product Name', validators=[DataRequired(), Length(max=100)])
    category_id = SelectField('Grocery Category', coerce=int, validators=[DataRequired()])
    unit = SelectField('Unit of Measurement', choices=[(u, u) for u in UNITS], validators=[DataRequired()])
    submit = SubmitField('Add Grocery Item')


class GroceryPurchaseForm(FlaskForm):
    purchase_date = DateField('Purchase Date', default=get_ist_date, validators=[DataRequired()])
    purchase_time = TimeField('Purchase Time', default=get_ist_time, validators=[DataRequired()])
    grocery_item_id = SelectField('Grocery Product', validators=[DataRequired()])
    quantity = DecimalField('Quantity', validators=[DataRequired(), NumberRange(min=0.01, message="Quantity must be greater than zero.")])
    unit_price = DecimalField('Unit Price (₹)', validators=[DataRequired(), NumberRange(min=0.01, message="Unit price must be positive.")])
    vendor_name = StringField('Vendor Name', validators=[Length(max=100)])
    notes = TextAreaField('Additional Notes')
    branch_id = SelectField('Branch', coerce=int)
    submit = SubmitField('Log Grocery Purchase')
