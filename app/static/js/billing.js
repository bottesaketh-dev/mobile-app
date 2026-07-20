document.addEventListener('DOMContentLoaded', function() {
    const orderId = document.getElementById('pos-order-id')?.value;
    if (!orderId) return;

    const cartList = document.getElementById('cart-items-list');
    const subtotalEl = document.getElementById('summary-subtotal');
    const taxEl = document.getElementById('summary-tax');
    const totalEl = document.getElementById('summary-total');
    
    // Fetch and render initial cart items
    loadCart();

    // Category pills filter trigger
    const categoryPills = document.querySelectorAll('.category-pill');
    categoryPills.forEach(pill => {
        pill.addEventListener('click', function() {
            categoryPills.forEach(p => p.classList.remove('active'));
            this.classList.add('active');
            
            const catName = this.dataset.category;
            const items = document.querySelectorAll('.pos-item-card');
            
            items.forEach(item => {
                if (catName === 'all' || item.dataset.category === catName) {
                    item.style.display = 'flex';
                } else {
                    item.style.display = 'none';
                }
            });
        });
    });

    // Item search functionality
    const itemSearch = document.getElementById('item-search');
    if (itemSearch) {
        itemSearch.addEventListener('input', function() {
            const query = this.value.toLowerCase().trim();
            const activePill = document.querySelector('.category-pill.active');
            const activeCat = activePill ? activePill.dataset.category : 'all';
            
            const items = document.querySelectorAll('.pos-item-card');
            items.forEach(item => {
                const name = item.dataset.name.toLowerCase();
                const matchesSearch = name.includes(query);
                const matchesCat = activeCat === 'all' || item.dataset.category === activeCat;
                
                if (matchesSearch && matchesCat) {
                    item.style.display = 'flex';
                } else {
                    item.style.display = 'none';
                }
            });
        });
    }

    // Add item click handler
    const itemCards = document.querySelectorAll('.pos-item-card');
    itemCards.forEach(card => {
        card.addEventListener('click', function() {
            const itemId = this.dataset.id;
            // Find if item already in cart, get current quantity and increment
            const currentItem = document.querySelector(`.cart-item[data-item-id="${itemId}"]`);
            const currentQty = currentItem ? parseInt(currentItem.dataset.qty) : 0;
            updateItemQuantity(itemId, currentQty + 1);
        });
    });

    // Load cart items from API
    function loadCart() {
        fetch(`/api/v1/billing/order/${orderId}/items`)
            .then(res => res.json())
            .then(items => {
                cartList.innerHTML = '';
                if (items.length === 0) {
                    cartList.innerHTML = `<div class="text-center text-muted py-5">Cart is empty.<br>Select items from the grid to add.</div>`;
                    updateTotalsDisplay(0, 0, 0);
                    return;
                }
                items.forEach(item => {
                    renderCartItem(item);
                });
                refreshTotals();
            });
    }

    // Render cart item in HTML
    function renderCartItem(item) {
        const itemEl = document.createElement('div');
        itemEl.className = 'cart-item';
        itemEl.dataset.itemId = item.menu_item_id;
        itemEl.dataset.qty = item.quantity;
        
        itemEl.innerHTML = `
            <div>
                <div class="fw-bold">${item.name}</div>
                <div class="text-muted small">₹${item.unit_price.toFixed(2)} x ${item.quantity}</div>
            </div>
            <div class="qty-control">
                <button class="qty-btn dec-btn" data-id="${item.menu_item_id}">-</button>
                <span class="fw-bold px-1">${item.quantity}</span>
                <button class="qty-btn inc-btn" data-id="${item.menu_item_id}">+</button>
            </div>
        `;
        
        // Bind button actions
        itemEl.querySelector('.inc-btn').addEventListener('click', function(e) {
            e.stopPropagation();
            updateItemQuantity(item.menu_item_id, item.quantity + 1);
        });
        
        itemEl.querySelector('.dec-btn').addEventListener('click', function(e) {
            e.stopPropagation();
            updateItemQuantity(item.menu_item_id, item.quantity - 1);
        });
        
        cartList.appendChild(itemEl);
    }

    // Call update API
    function updateItemQuantity(itemId, quantity) {
        const csrfToken = document.getElementById('csrf_token')?.value;
        
        fetch(`/api/v1/billing/order/${orderId}/update-item`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRFToken': csrfToken
            },
            body: JSON.stringify({
                menu_item_id: parseInt(itemId),
                quantity: quantity
            })
        })
        .then(res => res.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
                return;
            }
            loadCart();
        })
        .catch(err => console.error('Error updating item:', err));
    }

    // Refresh order totals from backend calculations
    function refreshTotals() {
        fetch(`/api/v1/billing/order/${orderId}/totals`)
            .then(res => res.json())
            .then(totals => {
                updateTotalsDisplay(totals.subtotal, totals.tax_amount, totals.total_amount);
            });
    }

    function updateTotalsDisplay(subtotal, tax, total) {
        subtotalEl.innerText = `₹${subtotal.toFixed(2)}`;
        taxEl.innerText = `₹${tax.toFixed(2)}`;
        totalEl.innerText = `₹${total.toFixed(2)}`;
    }
});
