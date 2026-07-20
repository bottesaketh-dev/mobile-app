from functools import wraps
from flask import abort, current_app
from flask_login import current_user

def role_required(*roles):
    """
    Decorator for views that checks if the user has a specific role.
    Should be placed AFTER @login_required decorator, or it will handle redirection automatically.
    """
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            if not current_user.is_authenticated:
                return current_app.login_manager.unauthorized()
            
            if current_user.role not in roles:
                abort(403)
                
            return f(*args, **kwargs)
        return decorated_function
    return decorator
