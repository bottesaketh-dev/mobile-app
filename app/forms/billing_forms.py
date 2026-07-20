from flask_wtf import FlaskForm
from wtforms import DecimalField, SelectField, TextAreaField, SubmitField
from wtforms.validators import NumberRange, InputRequired
from app.utils.constants import BILL_PAYMENT_MODES

class ProcessPaymentForm(FlaskForm):
    payment_mode = SelectField(
        'Payment Mode', 
        choices=[(mode, mode.upper()) for mode in BILL_PAYMENT_MODES], 
        validators=[InputRequired()]
    )
    discount_amount = DecimalField(
        'Discount (₹)', 
        default=0.00, 
        validators=[NumberRange(min=0.00, message="Discount must be non-negative.")]
    )
    notes = TextAreaField('Payment Notes / Details')
    submit = SubmitField('Process Payment')
