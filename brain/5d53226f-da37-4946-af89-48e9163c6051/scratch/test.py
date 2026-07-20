import requests
import json

url = "http://127.0.0.1:5000/chat/api/message"

# We need to bypass login or use a session
# Since login is required, this simple request will return 401 Unauthorized, but let's see.
# Actually, it's easier to check the server logs directly or test it properly.
