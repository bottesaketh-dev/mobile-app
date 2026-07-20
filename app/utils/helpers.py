from datetime import datetime, timedelta, timezone

def get_ist_now():
    """
    Returns the current date/time in Indian Standard Time (IST)
    as a timezone-naive datetime object (safe for standard SQL storage).
    """
    ist_tz = timezone(timedelta(hours=5, minutes=30))
    return datetime.now(ist_tz).replace(tzinfo=None)

def get_ist_date():
    return get_ist_now().date()

def get_ist_time():
    return get_ist_now().time()

def generate_bill_number(bills_today_count):
    """
    Generates bill numbers in format: INV-DDMMYYYY-XXX
    """
    date_str = get_ist_now().strftime('%d%m%Y')
    seq = bills_today_count + 1
    return f"INV-{date_str}-{seq:03d}"

def generate_order_number(orders_today_count):
    """
    Generates order numbers in format: ORD-DDMMYYYY-XXX
    """
    date_str = get_ist_now().strftime('%d%m%Y')
    seq = orders_today_count + 1
    return f"ORD-{date_str}-{seq:03d}"

def generate_employee_id(total_employees_count):
    """
    Generates employee IDs in format: EMP-001
    """
    seq = total_employees_count + 1
    return f"EMP-{seq:03d}"

def generate_grocery_product_id(category_name, items_in_category_count):
    """
    Generates grocery item product IDs in format: GRC-CAT-XXX
    Example: Vegetables -> GRC-VEG-001
    """
    # Clean the category name to a 3-letter prefix
    clean_cat = category_name.strip().upper()
    if clean_cat.startswith('VEG'):
        prefix = 'VEG'
    elif clean_cat.startswith('DAI'):
        prefix = 'DAI'
    elif clean_cat.startswith('SPI'):
        prefix = 'SPI'
    elif clean_cat.startswith('MEA'):
        prefix = 'MET'
    elif clean_cat.startswith('FRU'):
        prefix = 'FRU'
    elif clean_cat.startswith('PUL'):
        prefix = 'PLS'
    elif clean_cat.startswith('OIL'):
        prefix = 'OIL'
    elif clean_cat.startswith('GRA'):
        prefix = 'GRN'
    elif clean_cat.startswith('BEV'):
        prefix = 'BEV'
    else:
        prefix = clean_cat[:3]
        if len(prefix) < 3:
            prefix = prefix.ljust(3, 'X')
            
    seq = items_in_category_count + 1
    return f"GRC-{prefix}-{seq:03d}"
