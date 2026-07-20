import os
from app import create_app

# Read environment (default to development)
env = os.environ.get('FLASK_ENV', 'development')
app = create_app(env)

if __name__ == '__main__':
    # Listen on localhost by default for local development testing
    app.run(host='127.0.0.1', port=5000)
