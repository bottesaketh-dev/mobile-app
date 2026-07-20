import os
from datetime import timedelta
from dotenv import load_dotenv

# Load environmental configurations from .env
load_dotenv(override = True)

class Config:
    # Flask settings
    SECRET_KEY = os.environ.get('SECRET_KEY', 'default-weak-secret-key-12345')
    
    # Database
    db_url = os.environ.get('DATABASE_URL', 'sqlite:///restaurant_tracker.db')
    # Standardize older postgresql connection URI protocols
    if db_url.startswith("postgres://"):
        db_url = db_url.replace("postgres://", "postgresql://", 1)
    SQLALCHEMY_DATABASE_URI = db_url
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    
    # Sessions
    PERMANENT_SESSION_LIFETIME = timedelta(hours=8)
    
    # Rate Limiter
    RATELIMIT_DEFAULT = os.environ.get('RATELIMIT_DEFAULT', '200 per minute')
    
    # Localization
    TIMEZONE = os.environ.get('TIMEZONE', 'Asia/Kolkata')
    CURRENCY = os.environ.get('CURRENCY', 'INR')


class DevelopmentConfig(Config):
    DEBUG = True


class TestingConfig(Config):
    TESTING = True
    SQLALCHEMY_DATABASE_URI = 'sqlite:///:memory:'
    WTF_CSRF_ENABLED = False  # Disable CSRF for easier testing
    LOGIN_DISABLED = False


class ProductionConfig(Config):
    DEBUG = False
    # In production, require strong configuration checking
    pass


# Map environments to classes
config_by_name = {
    'development': DevelopmentConfig,
    'testing': TestingConfig,
    'production': ProductionConfig,
    'default': DevelopmentConfig
}
