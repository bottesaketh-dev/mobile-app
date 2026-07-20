from flask import Blueprint, jsonify, request
from flask_login import login_required, current_user
from app.models.order import Order, OrderItem
from app.services.billing_service import BillingService

api_billing_bp = Blueprint('api_billing', __name__)

@api_billing_bp.route('/order/<string:order_id>/items', methods=['GET'])
@login_required
def get_order_items(order_id):
    order = Order.query.get_or_404(order_id)
    if not current_user.is_owner and order.branch_id != current_user.branch_id:
        return jsonify({'error': 'Unauthorized branch access.'}), 403
        
    items = []
    for item in order.items:
        items.append({
            'menu_item_id': item.menu_item_id,
            'name': item.menu_item.name,
            'quantity': item.quantity,
            'unit_price': float(item.unit_price),
            'total_price': float(item.total_price),
            'notes': item.notes
        })
    return jsonify(items)


@api_billing_bp.route('/order/<string:order_id>/update-item', methods=['POST'])
@login_required
def update_item(order_id):
    order = Order.query.get_or_404(order_id)
    if not current_user.is_owner and order.branch_id != current_user.branch_id:
        return jsonify({'error': 'Unauthorized branch access.'}), 403
        
    data = request.get_json() or {}
    menu_item_id = data.get('menu_item_id')
    quantity = data.get('quantity')
    notes = data.get('notes', '')
    
    if menu_item_id is None or quantity is None:
        return jsonify({'error': 'Missing menu_item_id or quantity.'}), 400
        
    try:
        quantity = int(quantity)
        item = BillingService.update_order_item(order.order_id, menu_item_id, quantity, notes)
        totals = BillingService.calculate_order_totals(order)
        
        response = {
            'success': True,
            'totals': {
                'subtotal': float(totals['subtotal']),
                'tax_amount': float(totals['tax_amount']),
                'discount_amount': float(totals['discount_amount']),
                'total_amount': float(totals['total_amount'])
            }
        }
        
        if item:
            response['item'] = {
                'menu_item_id': item.menu_item_id,
                'name': item.menu_item.name,
                'quantity': item.quantity,
                'unit_price': float(item.unit_price),
                'total_price': float(item.total_price),
                'notes': item.notes
            }
        else:
            response['item'] = None # deleted
            
        return jsonify(response)
        
    except (ValueError, TypeError) as e:
        return jsonify({'error': str(e)}), 400


@api_billing_bp.route('/order/<string:order_id>/totals', methods=['GET'])
@login_required
def get_order_totals(order_id):
    order = Order.query.get_or_404(order_id)
    if not current_user.is_owner and order.branch_id != current_user.branch_id:
        return jsonify({'error': 'Unauthorized branch access.'}), 403
        
    discount = request.args.get('discount', 0.00, type=float)
    totals = BillingService.calculate_order_totals(order, discount)
    
    return jsonify({
        'subtotal': float(totals['subtotal']),
        'tax_amount': float(totals['tax_amount']),
        'discount_amount': float(totals['discount_amount']),
        'total_amount': float(totals['total_amount'])
    })
