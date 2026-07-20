import pytest
from app.models.user import User

def test_password_hashing(init_database):
    """
    Verifies passwords hash correctly using bcrypt and match validation routines.
    """
    user = User(username="hash_test", email="hash@test.com", role="biller")
    user.set_password("SecurePassword@123")
    
    assert user.password_hash != "SecurePassword@123"
    assert user.check_password("SecurePassword@123") is True
    assert user.check_password("WrongPassword") is False


def test_login_flow(client, init_database):
    """
    Validates staff login and access token returns.
    """
    # Fail Login (executed first while client is unauthenticated)
    res_fail = client.post('/auth/login', data={
        'username': 'test_biller',
        'password': 'WrongPassword'
    }, follow_redirects=True)
    assert res_fail.status_code == 200
    assert b"Invalid username or password" in res_fail.data

    # Success Login
    res = client.post('/auth/login', data={
        'username': 'test_biller',
        'password': 'Biller@123'
    }, follow_redirects=True)
    assert res.status_code == 200
    assert b"Welcome back" in res.data
    assert b"POS Dining Tables Grid" in res.data


def test_role_based_access(client, init_database):
    """
    Tests RBAC routing restrictions on admin pages.
    """
    # Unauthenticated access redirects to login
    res = client.get('/employees/')
    assert res.status_code == 302
    assert "/auth/login" in res.location

    # Biller login
    client.post('/auth/login', data={'username': 'test_biller', 'password': 'Biller@123'})
    
    # Biller tries to access employees list -> forbidden (403)
    res_biller = client.get('/employees/')
    assert res_biller.status_code == 403

    # Log out biller
    client.get('/auth/logout')

    # Owner login
    client.post('/auth/login', data={'username': 'test_owner', 'password': 'Owner@123'})
    
    # Owner tries to access employees list -> success (200)
    res_owner = client.get('/employees/')
    assert res_owner.status_code == 200
