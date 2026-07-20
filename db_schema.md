## Table `branches`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `branch_id` | `int4` | Primary |
| `name` | `varchar` |  |
| `address` | `text` |  Nullable |
| `phone` | `varchar` |  Nullable |
| `is_active` | `bool` |  |
| `created_at` | `timestamp` |  |

## Table `grocery_categories`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `grocery_category_id` | `int4` | Primary |
| `name` | `varchar` |  Unique |
| `description` | `text` |  Nullable |
| `is_active` | `bool` |  |

## Table `expense_categories`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `expense_category_id` | `int4` | Primary |
| `name` | `varchar` |  Unique |
| `description` | `text` |  Nullable |
| `is_active` | `bool` |  |

## Table `users`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `user_id` | `int4` | Primary |
| `username` | `varchar` |  Unique |
| `email` | `varchar` |  Unique |
| `password_hash` | `varchar` |  |
| `role` | `varchar` |  |
| `branch_id` | `int4` |  Nullable |
| `is_active` | `bool` |  |
| `created_at` | `timestamp` |  |
| `updated_at` | `timestamp` |  |

## Table `menu_items`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `menu_item_id` | `int4` | Primary |
| `name` | `varchar` |  |
| `description` | `text` |  Nullable |
| `category` | `varchar` |  |
| `price` | `numeric` |  |
| `is_vegetarian` | `bool` |  |
| `is_available` | `bool` |  |
| `image_url` | `varchar` |  Nullable |
| `branch_id` | `int4` |  |
| `created_at` | `timestamp` |  |
| `updated_at` | `timestamp` |  |

## Table `restaurant_tables`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `table_id` | `varchar` | Primary |
| `capacity` | `int4` |  |
| `status` | `varchar` |  |
| `branch_id` | `int4` |  |
| `is_active` | `bool` |  |

## Table `grocery_items`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `grocery_item_id` | `varchar` | Primary |
| `product_name` | `varchar` |  |
| `category_id` | `int4` |  |
| `unit` | `varchar` |  |
| `is_active` | `bool` |  |
| `created_at` | `timestamp` |  |

## Table `employees`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `employee_id` | `varchar` | Primary |
| `first_name` | `varchar` |  |
| `last_name` | `varchar` |  |
| `email` | `varchar` |  Nullable |
| `phone` | `varchar` |  |
| `position` | `varchar` |  |
| `monthly_salary` | `numeric` |  |
| `join_date` | `date` |  |
| `is_active` | `bool` |  |
| `branch_id` | `int4` |  |
| `created_at` | `timestamp` |  |
| `updated_at` | `timestamp` |  |

## Table `orders`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `order_id` | `varchar` | Primary |
| `table_id` | `varchar` |  |
| `status` | `varchar` |  |
| `created_by` | `int4` |  |
| `branch_id` | `int4` |  |
| `created_at` | `timestamp` |  |
| `updated_at` | `timestamp` |  |

## Table `grocery_purchases`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `grocery_purchase_id` | `int4` | Primary |
| `purchase_date` | `date` |  |
| `purchase_time` | `time` |  |
| `grocery_item_id` | `varchar` |  |
| `quantity` | `numeric` |  |
| `unit_price` | `numeric` |  |
| `total_price` | `numeric` |  |
| `vendor_name` | `varchar` |  Nullable |
| `notes` | `text` |  Nullable |
| `recorded_by` | `int4` |  |
| `branch_id` | `int4` |  |
| `created_at` | `timestamp` |  |

## Table `expenses`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `expense_id` | `int4` | Primary |
| `expense_date` | `date` |  |
| `expense_time` | `time` |  |
| `category_id` | `int4` |  |
| `description` | `varchar` |  |
| `amount` | `numeric` |  |
| `payment_mode` | `varchar` |  |
| `vendor_name` | `varchar` |  Nullable |
| `receipt_number` | `varchar` |  Nullable |
| `notes` | `text` |  Nullable |
| `recorded_by` | `int4` |  |
| `branch_id` | `int4` |  |
| `created_at` | `timestamp` |  |

## Table `salary_payments`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `salary_payment_id` | `int4` | Primary |
| `employee_id` | `varchar` |  |
| `payment_month` | `int4` |  |
| `payment_year` | `int4` |  |
| `base_salary` | `numeric` |  |
| `bonus` | `numeric` |  |
| `deductions` | `numeric` |  |
| `net_salary` | `numeric` |  |
| `payment_date` | `date` |  Nullable |
| `payment_status` | `varchar` |  |
| `payment_mode` | `varchar` |  Nullable |
| `processed_by` | `int4` |  |
| `branch_id` | `int4` |  |
| `created_at` | `timestamp` |  |

## Table `order_items`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `order_item_id` | `int4` | Primary |
| `order_id` | `varchar` |  |
| `menu_item_id` | `int4` |  |
| `quantity` | `int4` |  |
| `unit_price` | `numeric` |  |
| `total_price` | `numeric` |  |
| `notes` | `text` |  Nullable |
| `created_at` | `timestamp` |  |

## Table `bills`

### Columns

| Name | Type | Constraints |
|------|------|-------------|
| `bill_id` | `varchar` | Primary |
| `order_id` | `varchar` |  |
| `table_id` | `varchar` |  |
| `subtotal` | `numeric` |  |
| `tax_amount` | `numeric` |  |
| `discount_amount` | `numeric` |  |
| `total_amount` | `numeric` |  |
| `payment_mode` | `varchar` |  |
| `payment_status` | `varchar` |  |
| `billed_by` | `int4` |  |
| `branch_id` | `int4` |  |
| `bill_date` | `date` |  |
| `bill_time` | `time` |  |
| `created_at` | `timestamp` |  |
| `notes` | `text` |  Nullable |

