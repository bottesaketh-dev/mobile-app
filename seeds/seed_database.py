import os
import sys
from datetime import date, time, datetime
from decimal import Decimal

# Append parent directory to path so app can be imported
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from app import create_app
from app.extensions import db
from app.models.branch import Branch
from app.models.user import User
from app.models.table import RestaurantTable
from app.models.menu import MenuItem
from app.models.expense import ExpenseCategory, Expense
from app.models.grocery import GroceryCategory, GroceryItem, GroceryPurchase
from app.models.employee import Employee, SalaryPayment
from app.models.order import Order, OrderItem
from app.models.bill import Bill
from app.utils.helpers import generate_grocery_product_id, generate_bill_number

def seed():
    app = create_app()
    with app.app_context():
        print("Initializing database tables...")
        db.create_all()
        
        # 1. Seed Branch
        print("Seeding Branch...")
        branch = Branch.query.filter_by(name="Flavors of India - Connaught Place").first()
        if not branch:
            branch = Branch(
                name="Flavors of India - Connaught Place",
                address="H-Block, Radial Road 4, Connaught Place, New Delhi, Delhi 110001",
                phone="011-23456789"
            )
            db.session.add(branch)
            db.session.commit()
            print("Branch seeded.")
        else:
            print("Branch already exists.")
            
        # 2. Seed Users
        print("Seeding Staff Users...")
        default_users = [
            ("owner", "owner@restaurant.com", "Owner@123", "owner", None),
            ("manager", "manager@restaurant.com", "Manager@123", "branch_manager", branch.branch_id),
            ("biller", "biller@restaurant.com", "Biller@123", "biller", branch.branch_id)
        ]
        
        for username, email, pwd, role, b_id in default_users:
            user = User.query.filter_by(username=username).first()
            if not user:
                user = User(
                    username=username,
                    email=email,
                    role=role,
                    branch_id=b_id
                )
                user.set_password(pwd)
                db.session.add(user)
                print(f"User '{username}' created.")
        db.session.commit()
        
        # 3. Seed Tables
        print("Seeding Dining Tables...")
        table_configs = [
            ("1", 2), ("2", 4), ("3", 4), ("4", 6), ("5", 2),
            ("6", 4), ("7", 4), ("8", 8), ("9", 6), ("10", 2)
        ]
        for t_num, cap in table_configs:
            tbl = RestaurantTable.query.filter_by(table_id=t_num, branch_id=branch.branch_id).first()
            if not tbl:
                tbl = RestaurantTable(
                    table_id=t_num,
                    capacity=cap,
                    status='available',
                    branch_id=branch.branch_id
                )
                db.session.add(tbl)
        db.session.commit()
        print("Dining tables seeded.")

        # 4. Seed Menu Items
        print("Seeding Menu Items...")
        menu_items = [
            # Starters
            ("Paneer Tikka", "Clay-oven roasted cottage cheese chunks marinated in spices", "Starters", 280.00, True),
            ("Chicken Tikka", "Spiced boneless clay-oven baked chicken fillets", "Starters", 320.00, False),
            ("Veg Pakora", "Crispy deep-fried vegetables batter coatings", "Starters", 180.00, True),
            ("Samosa Chaat", "Deconstructed samosas topped with yogurt and chutneys", "Starters", 150.00, True),
            
            # Main Course
            ("Butter Chicken", "Classic chicken chunks cooked in silky tomato butter cream gravy", "Main Course", 420.00, False),
            ("Dal Makhani", "Slow cooked black lentils with cream and butter", "Main Course", 290.00, True),
            ("Paneer Butter Masala", "Cottage cheese cubes in rich tomato cashew gravy", "Main Course", 340.00, True),
            ("Kadai Vegetables", "Assorted vegetables wok tossed in fresh ground spices", "Main Course", 270.00, True),
            ("Traditional Chicken Curry", "Homestyle chicken curry cooked with onions and spices", "Main Course", 390.00, False),
            
            # Breads
            ("Butter Naan", "Flatbread baked in tandoor glazed with butter", "Breads", 60.00, True),
            ("Garlic Naan", "Tandoor baked flatbread infused with chopped garlic", "Breads", 80.00, True),
            ("Tandoori Roti", "Whole wheat tandoor baked round bread", "Breads", 30.00, True),
            ("Laccha Paratha", "Layered whole wheat pan baked flaky bread", "Breads", 70.00, True),
            
            # Rice
            ("Jeera Rice", "Basmati rice tempered with cumin seeds", "Rice", 140.00, True),
            ("Vegetable Biryani", "Aromatic basmati rice cooked with garden veg and herbs", "Rice", 260.00, True),
            ("Hyderabadi Chicken Biryani", "Fragrant layered basmati rice and spiced chicken cuts", "Rice", 350.00, False),
            ("Steamed Basmati Rice", "Fluffy long grain boiled basmati rice", "Rice", 110.00, True),
            
            # Beverages
            ("Sweet Lassi", "Chilled whipped yogurt drink with cardamom flavor", "Beverages", 90.00, True),
            ("Masala Chai", "Brewed milk tea with ginger and green cardamom spices", "Beverages", 50.00, True),
            ("Cold Drink", "Aerated sparkling soft beverage (Cola/Sprite)", "Beverages", 40.00, True),
            ("Fresh Lime Soda", "Fresh lime juice with soda water", "Beverages", 80.00, True),
            
            # Desserts
            ("Gulab Jamun", "Fried milk dough balls soaked in cardamom sugar syrup", "Desserts", 100.00, True),
            ("Rasmalai", "Flattened cottage cheese patties soaked in saffron milk syrup", "Desserts", 120.00, True),
            ("Kheer", "Traditional Indian rice pudding flavored with saffron and nuts", "Desserts", 90.00, True),
            ("Gajar Halwa", "Slow cooked grated carrots cooked with sweet condensed milk", "Desserts", 130.00, True)
        ]
        
        for name, desc, cat, price, is_veg in menu_items:
            item = MenuItem.query.filter_by(name=name, branch_id=branch.branch_id).first()
            if not item:
                item = MenuItem(
                    name=name,
                    description=desc,
                    category=cat,
                    price=price,
                    is_vegetarian=is_veg,
                    is_available=True,
                    branch_id=branch.branch_id
                )
                db.session.add(item)
        db.session.commit()
        print("Menu items seeded.")

        # 5. Seed Expense Categories
        print("Seeding Expense Classifications...")
        expense_cats = [
            ("Rent", "Monthly property lease payments"),
            ("Utilities", "Electricity, municipal water, commercial LPG cylinders costs"),
            ("Sanitary Products", "Kitchen detergents, soaps, table wipes, disinfectant cleaners"),
            ("Maintenance", "AC servicing, plumbing, repairs of kitchen grills"),
            ("Crockery", "Plates, bowls, customized serving platters"),
            ("Utensils", "Woks, cooking spoons, chef knives"),
            ("Marketing", "Pamphlet distribution, social media posts booster campaigns"),
            ("Miscellaneous", "Petty cash emergency costs")
        ]
        for name, desc in expense_cats:
            cat = ExpenseCategory.query.filter_by(name=name).first()
            if not cat:
                cat = ExpenseCategory(name=name, description=desc)
                db.session.add(cat)
        db.session.commit()
        print("Expense categories seeded.")

        # 6. Seed Grocery Categories
        print("Seeding Grocery Categories...")
        grocery_cats = [
            ("Vegetables", "Fresh green stock"),
            ("Fruits", "Fresh fruit supplies"),
            ("Meat", "Raw chicken, mutton, and fish cuts"),
            ("Pulses", "Lentils and beans stock"),
            ("Dairy Products", "Paneer, milk, curd, ghee, cream"),
            ("Spices", "Whole and powdered Indian seasonings"),
            ("Oils", "Cooking oil types"),
            ("Grains", "Rice, flour, refined maida"),
            ("Beverages", "Tea leaves, coffee powders, concentrates"),
            ("Others", "Sugar, salt, packing containers")
        ]
        for name, desc in grocery_cats:
            cat = GroceryCategory.query.filter_by(name=name).first()
            if not cat:
                cat = GroceryCategory(name=name, description=desc)
                db.session.add(cat)
        db.session.commit()
        print("Grocery categories seeded.")

        # 7. Seed 50+ Grocery Items
        print("Seeding 50+ Grocery Items profiles...")
        grocery_items_spec = {
            "Vegetables": [
                ("Tomato", "kg"), ("Onion", "kg"), ("Garlic", "kg"), ("Ginger", "kg"), 
                ("Potato", "kg"), ("Coriander Leaves", "kg"), ("Green Chilli", "kg"), 
                ("Lemon", "count"), ("Cauliflower", "kg"), ("Capsicum", "kg")
            ],
            "Dairy Products": [
                ("Cottage Cheese (Paneer)", "kg"), ("Full Cream Milk", "liters"), 
                ("Amul Butter Blocks", "count"), ("Desi Ghee Tin", "packet"), 
                ("Fresh Cream Can", "liters"), ("Curd / Yogurt Cask", "liters"), 
                ("Processed Cheese Grate", "kg")
            ],
            "Spices": [
                ("Turmeric Powder", "kg"), ("Kashmiri Red Chilli Powder", "kg"), 
                ("Garam Masala Powder", "kg"), ("Cumin Seeds", "kg"), 
                ("Coriander Powder", "kg"), ("Green Cardamom", "grams"), 
                ("Cloves Whole", "grams"), ("Cinnamon Sticks", "grams"), 
                ("Table Salt Packet", "packet"), ("Black Pepper Powder", "grams")
            ],
            "Pulses": [
                ("Black Gram Dal (Urad)", "kg"), ("Kidney Beans (Rajma)", "kg"), 
                ("Bengal Gram Dal (Chana)", "kg"), ("Yellow Moong Dal", "kg"), 
                ("Split Red Lentils (Masoor)", "kg")
            ],
            "Grains": [
                ("Long Grain Basmati Rice", "kg"), ("Whole Wheat Flour (Atta)", "kg"), 
                ("Refined Flour (Maida)", "kg"), ("Semolina (Suji)", "kg")
            ],
            "Oils": [
                ("Mustard Oil Jars", "liters"), ("Refined Sunflower Oil Jars", "liters"), 
                ("Pure Sesame Oil", "liters")
            ],
            "Fruits": [
                ("Ripe Alphonso Mangoes", "dozen"), ("Fresh Apples", "kg"), 
                ("Bananas Ripe", "dozen")
            ],
            "Meat": [
                ("Boneless Chicken Breast Cuts", "kg"), ("Raw Lean Mutton cuts", "kg"), 
                ("River Basa Fish Fillets", "kg")
            ],
            "Beverages": [
                ("Assam CTC Tea Leaves", "packet"), ("Instant Coffee Powder", "packet"), 
                ("Lemon Juice Concentrate", "liters")
            ],
            "Others": [
                ("Refined White Sugar", "kg"), ("Takeaway Plastic Containers", "count"), 
                ("Paper Table Napkins", "packet"), ("Vinegar Cans", "liters"), 
                ("Soy Sauce Bottles", "count")
            ]
        }
        
        for cat_name, items_list in grocery_items_spec.items():
            cat = GroceryCategory.query.filter_by(name=cat_name).first()
            if not cat:
                continue
                
            for idx, (p_name, unit) in enumerate(items_list):
                item = GroceryItem.query.filter_by(product_name=p_name).first()
                if not item:
                    pid = generate_grocery_product_id(cat.name, idx)
                    item = GroceryItem(
                        grocery_item_id=pid,
                        product_name=p_name,
                        category_id=cat.grocery_category_id,
                        unit=unit
                    )
                    db.session.add(item)
        db.session.commit()
        print("Grocery profiles created.")

        # 8. Seed Employees
        print("Seeding Staff Employees...")
        employees_spec = [
            ("EMP-001", "Amit", "Sharma", "amit@restaurant.com", "9876543210", "Chef", 35000.00, date(2024, 1, 15)),
            ("EMP-002", "Priya", "Patel", "priya@restaurant.com", "9876543211", "Cashier", 22000.00, date(2024, 3, 10)),
            ("EMP-003", "Rahul", "Singh", "rahul@restaurant.com", "9876543212", "Waiter", 18000.00, date(2024, 6, 1)),
            ("EMP-004", "Sunita", "Devi", "sunita@restaurant.com", "9876543213", "Cleaner", 12000.00, date(2025, 2, 1))
        ]
        
        seeded_emps = []
        for emp_id, f_name, l_name, email, phone, pos, sal, join_d in employees_spec:
            emp = Employee.query.filter_by(employee_id=emp_id).first()
            if not emp:
                emp = Employee(
                    employee_id=emp_id,
                    first_name=f_name,
                    last_name=l_name,
                    email=email,
                    phone=phone,
                    position=pos,
                    monthly_salary=sal,
                    join_date=join_d,
                    branch_id=branch.branch_id
                )
                db.session.add(emp)
                db.session.commit()
            else:
                emp.monthly_salary = Decimal(str(sal))
                db.session.commit()
            seeded_emps.append(emp)
        print("Staff Employees seeded.")

        # 9. Seed Salary Payments (outflow for last month, e.g. paid in June 2026)
        print("Seeding Staff Salary Payments...")
        for emp in seeded_emps:
            pay = SalaryPayment.query.filter_by(employee_id=emp.employee_id, payment_month=5, payment_year=2026).first()
            if not pay:
                bonus = Decimal('1000.00') if emp.position == "Chef" else Decimal('0.00')
                deductions = Decimal('500.00') if emp.position == "Waiter" else Decimal('0.00')
                net = emp.monthly_salary + bonus - deductions
                pay = SalaryPayment(
                    employee_id=emp.employee_id,
                    payment_month=5,
                    payment_year=2026,
                    base_salary=emp.monthly_salary,
                    bonus=bonus,
                    deductions=deductions,
                    net_salary=net,
                    payment_date=date(2026, 6, 5),
                    payment_status='paid',
                    payment_mode='bank_transfer',
                    processed_by=1, # owner
                    branch_id=branch.branch_id
                )
                db.session.add(pay)
        db.session.commit()
        print("Salary payments seeded.")

        # 10. Seed Grocery Purchase Logs (outflow)
        print("Seeding Grocery Purchases...")
        purchases_spec = [
            (date(2026, 6, 2), time(10, 30), 12.0, 320.0, "Metro Cash & Carry", "Paneer block"),
            (date(2026, 6, 8), time(11, 0), 60.0, 35.0, "Local Sabzi Mandi", "Onions"),
            (date(2026, 6, 15), time(9, 15), 5.0, 850.0, "Desi Oils Ltd", "Desi Ghee Tin"),
            (date(2026, 6, 22), time(10, 0), 25.0, 130.0, "Long Grain Rice Co", "Long Grain Basmati Rice"),
            (date(2026, 6, 28), time(11, 45), 18.0, 240.0, "Chicken Poultry Farm", "Boneless Chicken Breast Cuts")
        ]
        for p_date, p_time, qty, u_price, vendor, p_name in purchases_spec:
            item = GroceryItem.query.filter_by(product_name=p_name).first()
            if item:
                gp = GroceryPurchase.query.filter_by(purchase_date=p_date, grocery_item_id=item.grocery_item_id).first()
                if not gp:
                    qty_dec = Decimal(str(qty))
                    price_dec = Decimal(str(u_price))
                    gp = GroceryPurchase(
                        purchase_date=p_date,
                        purchase_time=p_time,
                        grocery_item_id=item.grocery_item_id,
                        quantity=qty_dec,
                        unit_price=price_dec,
                        total_price=qty_dec * price_dec,
                        vendor_name=vendor,
                        notes=f"Procured raw {p_name}",
                        recorded_by=1,
                        branch_id=branch.branch_id
                    )
                    db.session.add(gp)
        db.session.commit()
        print("Grocery purchase logs seeded.")

        # 11. Seed General Expenses (outflow)
        print("Seeding General Expenses...")
        rent_cat = ExpenseCategory.query.filter_by(name="Rent").first()
        util_cat = ExpenseCategory.query.filter_by(name="Utilities").first()
        maint_cat = ExpenseCategory.query.filter_by(name="Maintenance").first()
        
        expenses_spec = [
            (date(2026, 6, 1), time(10, 0), rent_cat.expense_category_id if rent_cat else 1, "Connaught Place Monthly Lease", 85000.00, "bank_transfer", "DLF Properties", "REC-8509"),
            (date(2026, 6, 10), time(14, 30), util_cat.expense_category_id if util_cat else 2, "Electricity bill June 2026", 14200.00, "upi", "NDPL", "REC-9872"),
            (date(2026, 6, 18), time(12, 15), maint_cat.expense_category_id if maint_cat else 4, "Tandoor clay wall restoration", 3500.00, "cash", "Ramesh Mason", "REC-3329")
        ]
        for exp_date, exp_time, cat_id, desc, amt, pay_m, vendor, rec_num in expenses_spec:
            exp = Expense.query.filter_by(expense_date=exp_date, category_id=cat_id).first()
            if not exp:
                exp = Expense(
                    expense_date=exp_date,
                    expense_time=exp_time,
                    category_id=cat_id,
                    description=desc,
                    amount=Decimal(str(amt)),
                    payment_mode=pay_m,
                    vendor_name=vendor,
                    receipt_number=rec_num,
                    notes="Approved branch expense",
                    recorded_by=1,
                    branch_id=branch.branch_id
                )
                db.session.add(exp)
        db.session.commit()
        print("General expenses seeded.")

        # 12. Seed Customer Orders and settled Bills (inflow)
        print("Seeding Customer Billings (Inflow)...")
        bills_spec = [
            (date(2026, 6, 5), time(13, 0), "1", [("Paneer Tikka", 2), ("Garlic Naan", 4), ("Sweet Lassi", 4)], "upi", 0.0),
            (date(2026, 6, 12), time(20, 30), "2", [("Butter Chicken", 2), ("Garlic Naan", 6), ("Cold Drink", 4), ("Gulab Jamun", 4)], "card", 50.0),
            (date(2026, 6, 19), time(14, 15), "3", [("Vegetable Biryani", 3), ("Sweet Lassi", 3), ("Rasmalai", 3)], "cash", 20.0),
            (date(2026, 6, 25), time(21, 0), "4", [("Paneer Butter Masala", 1), ("Dal Makhani", 1), ("Butter Naan", 4), ("Jeera Rice", 1), ("Rasmalai", 2)], "upi", 0.0),
            (date(2026, 6, 29), time(19, 45), "5", [("Chicken Tikka", 2), ("Butter Chicken", 1), ("Garlic Naan", 4), ("Sweet Lassi", 2)], "upi", 100.0)
        ]
        
        for idx, (b_date, b_time, table_num, items_list, pay_mode, discount) in enumerate(bills_spec):
            bill_num = f"INV-{b_date.strftime('%d%m%Y')}-{idx+1:03d}"
            bill = Bill.query.filter_by(bill_id=bill_num).first()
            if not bill:
                table = RestaurantTable.query.filter_by(table_id=table_num, branch_id=branch.branch_id).first()
                order_num = f"ORD-{b_date.strftime('%d%m%Y')}-{idx+1:03d}"
                dt_local = datetime.combine(b_date, b_time)
                
                order = Order(
                    order_id=order_num,
                    table_id=table.table_id,
                    status='completed',
                    created_by=3, # biller user ID
                    branch_id=branch.branch_id,
                    created_at=dt_local
                )
                db.session.add(order)
                db.session.flush() # flush to get order.id
                
                subtotal = Decimal('0.00')
                tax_amount = Decimal('0.00')
                
                for name, qty in items_list:
                    dish = MenuItem.query.filter_by(name=name, branch_id=branch.branch_id).first()
                    if dish:
                        price = Decimal(str(dish.price))
                        total_item_price = price * Decimal(str(qty))
                        
                        item = OrderItem(
                            order_id=order.order_id,
                            menu_item_id=dish.menu_item_id,
                            quantity=qty,
                            unit_price=price,
                            total_price=total_item_price
                        )
                        db.session.add(item)
                        
                        subtotal += total_item_price
                        # GST calculation
                        from app.utils.constants import GST_RATES, DEFAULT_GST_RATE
                        rate = GST_RATES.get(dish.category, DEFAULT_GST_RATE)
                        tax_amount += total_item_price * Decimal(str(rate))
                        
                discount_dec = Decimal(str(discount))
                grand_total = subtotal + tax_amount - discount_dec
                
                bill = Bill(
                    bill_id=bill_num,
                    order_id=order.order_id,
                    table_id=table.table_id,
                    subtotal=subtotal,
                    tax_amount=tax_amount,
                    discount_amount=discount_dec,
                    total_amount=grand_total,
                    payment_mode=pay_mode,
                    payment_status='paid',
                    billed_by=3,
                    branch_id=branch.branch_id,
                    bill_date=b_date,
                    bill_time=b_time,
                    created_at=dt_local
                )
                db.session.add(bill)
        db.session.commit()
        print("Customer orders and settled bills seeded.")
        print("Database seeding completed successfully.")

if __name__ == '__main__':
    seed()
