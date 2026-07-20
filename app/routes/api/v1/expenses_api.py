from flask import Blueprint, jsonify, request
from flask_login import login_required, current_user
from app.models.grocery import GroceryItem, GroceryCategory

api_expenses_bp = Blueprint('api_expenses', __name__)

@api_expenses_bp.route('/groceries/search', methods=['GET'])
@login_required
def search_grocery_items():
    """
    Search grocery items by product name (autocomplete endpoint).
    """
    query = request.args.get('q', '').strip()
    if not query:
        return jsonify([])
        
    items = GroceryItem.query.filter(
        GroceryItem.product_name.ilike(f"%{query}%"),
        GroceryItem.is_active == True
    ).limit(10).all()
    
    result = []
    for item in items:
        result.append({
            'id': item.grocery_item_id,
            'product_id': item.grocery_item_id,
            'product_name': item.product_name,
            'unit': item.unit,
            'category': item.category.name
        })
        
    return jsonify(result)


@api_expenses_bp.route('/groceries/category/<int:category_id>', methods=['GET'])
@login_required
def get_groceries_by_category(category_id):
    """
    Returns grocery items in a specific category.
    """
    category = GroceryCategory.query.get_or_404(category_id)
    items = GroceryItem.query.filter_by(category_id=category.grocery_category_id, is_active=True).all()
    
    result = []
    for item in items:
        result.append({
            'id': item.grocery_item_id,
            'product_id': item.grocery_item_id,
            'product_name': item.product_name,
            'unit': item.unit
        })
        
    return jsonify(result)
