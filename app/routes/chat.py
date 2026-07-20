from flask import Blueprint, render_template, request, jsonify, Response, stream_with_context
from flask_login import login_required
from app.services.ai.engine import RestaurantAgent

chat_bp = Blueprint('chat', __name__)

agent = None

def get_agent():
    global agent
    if agent is None:
        agent = RestaurantAgent()
    return agent

@chat_bp.route('/')
@login_required
def index():
    return render_template('chat/index.html')

@chat_bp.route('/api/message', methods=['POST'])
@login_required
def message():
    user_input = request.json.get('prompt')
    if not user_input:
        return jsonify({"error": "Prompt is required"}), 400

    try:
        ag = get_agent()
        resp = Response(
            stream_with_context(ag.stream_process_query(user_input)),
            mimetype='application/x-ndjson',
        )
        # Prevent any layer from buffering the streamed chunks
        resp.headers['Cache-Control'] = 'no-cache'
        resp.headers['X-Accel-Buffering'] = 'no'
        resp.headers['Transfer-Encoding'] = 'chunked'
        return resp
    except Exception as e:
        return jsonify({"error": str(e)}), 500

