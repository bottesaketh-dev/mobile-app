from flask_wtf import FlaskForm
from wtforms import StringField, TextAreaField, BooleanField, SubmitField
from wtforms.validators import DataRequired, Length

class BranchForm(FlaskForm):
    name = StringField('Branch Name', validators=[DataRequired(), Length(max=100)])
    address = TextAreaField('Address', validators=[Length(max=500)])
    phone = StringField('Phone Number', validators=[Length(max=15)])
    is_active = BooleanField('Active', default=True)
    submit = SubmitField('Save Branch')
