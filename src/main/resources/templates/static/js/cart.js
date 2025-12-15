/* ==================== SHOPPING CART FUNCTIONS ==================== */

let cartItems = JSON.parse(localStorage.getItem('cart')) || [];

// Add to Cart
function addToCart(productId, productName, price, image) {
  const existingItem = cartItems.find(item => item.id === productId);

  if (existingItem) {
    existingItem.quantity += 1;
  } else {
    cartItems.push({
      id: productId,
      name: productName,
      price: parseFloat(price),
      image: image,
      quantity: 1,
    });
  }

  saveCart();
  campusMarketplace.showToast(`${productName} added to cart!`, 'success');
  updateCartCount();
  updateCartUI();
}

// Remove from Cart
function removeFromCart(productId) {
  cartItems = cartItems.filter(item => item.id !== productId);
  saveCart();
  campusMarketplace.showToast('Item removed from cart', 'success');
  updateCartCount();
  updateCartUI();
}

// Update Quantity
function updateQuantity(productId, quantity) {
  const item = cartItems.find(item => item.id === productId);
  if (item) {
    item.quantity = Math.max(1, parseInt(quantity));
    saveCart();
    updateCartUI();
  }
}

// Clear Cart
function clearCart() {
  cartItems = [];
  saveCart();
  campusMarketplace.showToast('Cart cleared', 'success');
  updateCartCount();
  updateCartUI();
}

// Save Cart to LocalStorage
function saveCart() {
  localStorage.setItem('cart', JSON.stringify(cartItems));
}

// Get Cart Total
function getCartTotal() {
  return cartItems.reduce((total, item) => total + item.price * item.quantity, 0);
}

// Get Cart Item Count
function getCartCount() {
  return cartItems.reduce((count, item) => count + item.quantity, 0);
}

// Update Cart Count in UI
function updateCartCount() {
  const cartBadges = document.querySelectorAll('.cart-count');
  const count = getCartCount();
  cartBadges.forEach(badge => {
    badge.textContent = count;
  });
}

// Update Cart UI
function updateCartUI() {
  const cartContainer = document.getElementById('cart-items-container');
  const cartTotal = document.getElementById('cart-total');

  if (cartContainer) {
    if (cartItems.length === 0) {
      cartContainer.innerHTML = '<div class="alert alert-info">Your cart is empty</div>';
    } else {
      cartContainer.innerHTML = cartItems
        .map(
          item => `
        <div class="cart-item" data-product-id="${item.id}">
          <div class="row align-items-center">
            <div class="col-md-2">
              <img src="${item.image}" alt="${item.name}" class="img-fluid rounded">
            </div>
            <div class="col-md-3">
              <h6>${item.name}</h6>
              <small class="text-muted">${campusMarketplace.formatCurrency(item.price)}</small>
            </div>
            <div class="col-md-2">
              <input type="number" class="form-control quantity-input" value="${item.quantity}" 
                     min="1" onchange="updateQuantity(${item.id}, this.value)">
            </div>
            <div class="col-md-2">
              <p class="mb-0 font-weight-bold">${campusMarketplace.formatCurrency(item.price * item.quantity)}</p>
            </div>
            <div class="col-md-3 text-right">
              <button class="btn btn-sm btn-danger" onclick="removeFromCart(${item.id})">
                <i class="fa fa-trash"></i> Remove
              </button>
            </div>
          </div>
        </div>
      `
        )
        .join('');
    }
  }

  if (cartTotal) {
    cartTotal.textContent = campusMarketplace.formatCurrency(getCartTotal());
  }
}

// Checkout
function proceedToCheckout() {
  if (cartItems.length === 0) {
    campusMarketplace.showToast('Your cart is empty', 'danger');
    return;
  }

  window.location.href = 'checkout.html';
}

// Apply Coupon
function applyCoupon(couponCode) {
  // Mock coupon validation
  const coupons = {
    WELCOME10: 0.1,
    SAVE20: 0.2,
    FREESHIP: 0.05,
  };

  if (coupons[couponCode]) {
    const discount = getCartTotal() * coupons[couponCode];
    const newTotal = getCartTotal() - discount;

    const discountElement = document.getElementById('discount-amount');
    const totalElement = document.getElementById('cart-total-with-discount');

    if (discountElement) {
      discountElement.textContent = campusMarketplace.formatCurrency(discount);
    }
    if (totalElement) {
      totalElement.textContent = campusMarketplace.formatCurrency(newTotal);
    }

    campusMarketplace.showToast(`Coupon "${couponCode}" applied! Discount: ${campusMarketplace.formatCurrency(discount)}`, 'success');
    return discount;
  } else {
    campusMarketplace.showToast('Invalid coupon code', 'danger');
    return 0;
  }
}

// Initialize Cart on Page Load
document.addEventListener('DOMContentLoaded', function() {
  updateCartCount();
  updateCartUI();

  // Add to cart button listeners
  document.querySelectorAll('.add-to-cart-btn').forEach(btn => {
    btn.addEventListener('click', function() {
      const productId = this.dataset.productId;
      const productName = this.dataset.productName;
      const price = this.dataset.price;
      const image = this.dataset.image;

      addToCart(productId, productName, price, image);
    });
  });

  // Clear cart button
  const clearCartBtn = document.getElementById('clear-cart-btn');
  if (clearCartBtn) {
    clearCartBtn.addEventListener('click', function() {
      if (confirm('Are you sure you want to clear the cart?')) {
        clearCart();
      }
    });
  }

  // Coupon form
  const couponForm = document.getElementById('coupon-form');
  if (couponForm) {
    couponForm.addEventListener('submit', function(e) {
      e.preventDefault();
      const couponInput = document.getElementById('coupon-code');
      applyCoupon(couponInput.value);
    });
  }

  // Checkout button
  const checkoutBtn = document.getElementById('checkout-btn');
  if (checkoutBtn) {
    checkoutBtn.addEventListener('click', proceedToCheckout);
  }
});

// Export for other scripts
window.cartModule = {
  addToCart,
  removeFromCart,
  updateQuantity,
  clearCart,
  getCartTotal,
  getCartCount,
  applyCoupon,
  proceedToCheckout,
};
