from flask_wtf import FlaskForm
from wtforms import StringField, TextAreaField, DecimalField, BooleanField, SelectField, SubmitField
from wtforms.validators import DataRequired, NumberRange, Length
from app.utils.constants import MENU_CATEGORIES

class MenuItemForm(FlaskForm):
    name = StringField('Item Name', validators=[DataRequired(), Length(max=100)])
    description = TextAreaField('Description', validators=[Length(max=500)])
    category = SelectField('Category', choices=[(cat, cat) for cat in MENU_CATEGORIES], validators=[DataRequired()])
    price = DecimalField('Price (₹)', validators=[DataRequired(), NumberRange(min=0.01, message="Price must be positive.")])
    is_vegetarian = BooleanField('Vegetarian')
    is_available = BooleanField('Available', default=True)
    image_url = StringField('Image URL', validators=[Length(max=255)])
    branch_id = SelectField('Branch', coerce=int, validators=[DataRequired()])
    submit = SubmitField('Save Menu Item')
