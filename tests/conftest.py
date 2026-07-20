import os
import sys
import pytest
from datetime import date

# Append root folder to path so app can be imported
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from app import create_app
from app.extensions import db as _db
from app.models.branch import Branch
from app.models.user import User
from app.models.table import RestaurantTable
from app.models.menu import MenuItem
from app.models.expense import ExpenseCategory

@pytest.fixture(scope='session')
def app():
    """
    Creates and configures a new Flask app instance for testing.
    """
    app = create_app('testing')
    return app

@pytest.fixture(scope='function')
def client(app):
    """
    Returns a Flask test client.
    """
    return app.test_client()

@pytest.fixture(scope='function')
def init_database(app):
    """
    Sets up an in-memory database, seeds baseline data, and tears down on test completion.
    """
    with app.app_context():
        # Setup tables
        _db.create_all()
        
        # Seed Branch
        branch = Branch(
            name="Test Branch",
            address="Test Address",
            phone="123456"
        )
        _db.session.add(branch)
        _db.session.commit()
        
        # Seed Users
        owner = User(username="test_owner", email="owner@test.com", role="owner")
        owner.set_password("Owner@123")
        
        manager = User(username="test_manager", email="manager@test.com", role="branch_manager", branch_id=branch.branch_id)
        manager.set_password("Manager@123")
        
        biller = User(username="test_biller", email="biller@test.com", role="biller", branch_id=branch.branch_id)
        biller.set_password("Biller@123")
        
        _db.session.add_all([owner, manager, biller])
        
        # Seed Table
        table = RestaurantTable(table_id="T1", capacity=4, status="available", branch_id=branch.branch_id)
        _db.session.add(table)
        
        # Seed Menu Category & Item
        item = MenuItem(
            name="Paneer Tikka",
            description="Cottage cheese",
            category="Starters",
            price=250.00,
            is_vegetarian=True,
            is_available=True,
            branch_id=branch.id
        )
        _db.session.add(item)
        
        # Seed Expense Category
        cat = ExpenseCategory(name="Utilities", description="Utility bills")
        _db.session.add(cat)
        
        _db.session.commit()
        
        yield _db
        
        # Clean database
        _db.session.remove()
        _db.drop_all()
