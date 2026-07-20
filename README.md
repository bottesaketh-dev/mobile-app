# Restaurant Revenue Tracking Application (Flavors Ledger)

A production-grade, multi-role restaurant management and revenue tracking system for an Indian restaurant. It tracks cash inflow (POS billing, order placement, table management) and cash outflow (grocery procurement, staff salary payroll, and general operational expenses) with role-based access controls (RBAC) and high-quality charts.

---

## Technical Stack
- **Backend**: Python 3.11+, Flask Web Framework
- **ORM & Database**: SQLAlchemy with support for PostgreSQL (Production) and SQLite (Development/Test)
- **Frontend**: Jinja2 Templates, Bootstrap 5, Custom theme stylesheets, Chart.js for graphs
- **Security**: Flask-WTF (CSRF), Bcrypt (Password hashing), Flask-Limiter (Rate limiting)
- **Reporting**: ReportLab PDF library for printable customer receipts

---

## Folder Structure
The project is organized in a modular structure:
- `app/`: Primary application source.
  - `models/`: Database schema files (ORM).
  - `routes/`: Blueprint controller routes, including `/api/v1/` REST endpoints.
  - `services/`: Business logic scripts (POS logic, aggregates reports).
  - `forms/`: WTForms definitions with validation checks.
  - `static/`: Custom CSS styling rules, AJAX POS cart handlers, and Chart.js settings.
  - `templates/`: Jinja2 templates (dashboards, tables grids, error panels).
- `seeds/`: Initial db tables creations and default data populator.
- `tests/`: Automated pytest suites.

---

## Getting Started (Local Setup)

### 1. Prerequisite Packages
Make sure you have Python 3.11+ installed. Create a virtual environment and activate it:
```bash
python -m venv venv
# On Windows PowerShell:
.\venv\Scripts\Activate.ps1
```

Install the dependencies:
```bash
pip install -r requirements.txt
```

### 2. Configure Environment `.env`
Create a `.env` file by copying the template. By default, it is configured to use a local SQLite database file `restaurant_tracker.db` to make development setup immediate:
```bash
copy .env.example .env
```

### 3. Initialize and Seed Database
Run the consolidate seeder script to create database tables and pre-populate them with default staff credentials, 10 dining tables, 20+ typical Indian restaurant dishes, and 50+ grocery stock items:
```bash
python seeds/seed_database.py
```

### 4. Run Development Server
Start the local server:
```bash
python wsgi.py
```
Open your browser and navigate to `http://127.0.0.1:5000/`.

---

## Default Login Credentials
Use the following accounts to test the Role-Based Access Control matrix:

| Username | Email | Password | Assigned Role | Permissions |
| :--- | :--- | :--- | :--- | :--- |
| **owner** | owner@restaurant.com | `Owner@123` | **Owner** | Full dashboard view, Staff directories, Payroll settlement, cashflow statement views, Menu CRUD, POS billing |
| **manager** | manager@restaurant.com | `Manager@123` | **Branch Manager** | Local branch dashboard, log grocery purchases, log general expenses, toggle menu availability |
| **biller** | biller@restaurant.com | `Biller@123` | **Biller / Cashier** | Table grids panel, POS cart orders creation, settlement checking, receipt printings |

---

## Running Automated Tests
Run the test suites with code coverage using pytest:
```bash
pytest --cov=app tests/
```

---

## Running Containerized (Docker)
Ensure Docker and Docker Compose are installed, then spin up the Flask web container and the PostgreSQL database container together:
```bash
docker-compose up --build
```
The application will run on port `5000` connected to a local Postgres container.
To seed the docker Postgres database, log inside the container and invoke the seeder:
```bash
docker exec -it restaurant_flask_web python seeds/seed_database.py
```
