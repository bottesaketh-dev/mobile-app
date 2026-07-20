from flask import Blueprint, jsonify
from flask_login import login_required, current_user

api_auth_bp = Blueprint('api_auth', __name__)

@api_auth_bp.route('/session', methods=['GET'])
@login_required
def get_session():
    """
    Returns current authenticated user details.
    """
    return jsonify({
        'id': current_user.user_id,
        'username': current_user.username,
        'email': current_user.email,
        'role': current_user.role,
        'branch_id': current_user.branch_id
    })
