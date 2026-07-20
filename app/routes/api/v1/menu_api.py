from flask import Blueprint, jsonify, request
from flask_login import login_required, current_user
from app.models.menu import MenuItem

api_menu_bp = Blueprint('api_menu', __name__)

@api_menu_bp.route('/items', methods=['GET'])
@login_required
def get_menu_items():
    """
    Returns menu items in JSON format, filtered by branch and availability.
    """
    branch_id = current_user.branch_id
    if not branch_id:
        branch_id = request.args.get('branch_id', type=int)
        
    if not branch_id:
        return jsonify({'error': 'Branch ID is required.'}), 400
        
    category = request.args.get('category', '').strip()
    
    query = MenuItem.query.filter_by(branch_id=branch_id, is_available=True)
    if category:
        query = query.filter_by(category=category)
        
    items = query.order_by(MenuItem.name).all()
    
    result = []
    for item in items:
        result.append({
            'id': item.menu_item_id,
            'name': item.name,
            'description': item.description,
            'category': item.category,
            'price': float(item.price),
            'is_vegetarian': item.is_vegetarian,
            'image_url': item.image_url
        })
        
    return jsonify(result)
