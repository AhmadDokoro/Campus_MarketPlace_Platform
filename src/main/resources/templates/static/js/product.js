/* ==================== PRODUCT FUNCTIONS ==================== */

// Product Filter
function filterProducts(category = null, sortBy = null) {
  const products = document.querySelectorAll('.product-item');

  products.forEach(product => {
    let show = true;

    if (category && category !== 'all') {
      const productCategory = product.dataset.category;
      show = productCategory === category;
    }

    product.style.display = show ? 'block' : 'none';
  });

  if (sortBy) {
    sortProductsBy(sortBy);
  }
}

// Sort Products
function sortProductsBy(sortBy) {
  const container = document.querySelector('.products-container');
  if (!container) return;

  const products = Array.from(container.querySelectorAll('.product-item:not([style*="display: none"])'));

  products.sort((a, b) => {
    const priceA = parseFloat(a.dataset.price || 0);
    const priceB = parseFloat(b.dataset.price || 0);
    const nameA = a.dataset.name || '';
    const nameB = b.dataset.name || '';

    switch (sortBy) {
      case 'price-asc':
        return priceA - priceB;
      case 'price-desc':
        return priceB - priceA;
      case 'name-asc':
        return nameA.localeCompare(nameB);
      case 'name-desc':
        return nameB.localeCompare(nameA);
      case 'newest':
        return (b.dataset.id || 0) - (a.dataset.id || 0);
      default:
        return 0;
    }
  });

  products.forEach(product => {
    container.appendChild(product);
  });
}

// Image Gallery
class ImageGallery {
  constructor(mainImageSelector, thumbnailSelector) {
    this.mainImage = document.querySelector(mainImageSelector);
    this.thumbnails = document.querySelectorAll(thumbnailSelector);

    this.initEvents();
  }

  initEvents() {
    this.thumbnails.forEach(thumb => {
      thumb.addEventListener('click', e => this.selectImage(e));
      thumb.addEventListener('keydown', e => {
        if (e.key === 'Enter') this.selectImage(e);
      });
    });
  }

  selectImage(event) {
    const thumb = event.currentTarget;
    const src = thumb.dataset.src || thumb.src;
    const alt = thumb.alt || 'Product Image';

    this.mainImage.src = src;
    this.mainImage.alt = alt;

    // Update active state
    this.thumbnails.forEach(t => t.classList.remove('active'));
    thumb.classList.add('active');
  }
}

// Product Quick View
function openQuickView(productId) {
  const modal = document.getElementById('quickViewModal');
  if (!modal) {
    console.error('Quick view modal not found');
    return;
  }

  // Fetch product data (mock)
  const products = {
    1: {
      id: 1,
      name: 'Product Name 1',
      price: 123.0,
      originalPrice: 150.0,
      rating: 4.5,
      reviews: 99,
      image: 'img/product-1.jpg',
      description: 'High-quality product with excellent features',
      inStock: true,
    },
    // Add more products as needed
  };

  const product = products[productId] || products[1];
  const content = `
    <div class="row">
      <div class="col-md-6">
        <img src="${product.image}" alt="${product.name}" class="img-fluid" />
      </div>
      <div class="col-md-6">
        <h3>${product.name}</h3>
        <div class="rating mb-3">
          <span class="text-warning">
            ${Array.from({ length: Math.round(product.rating) })
              .map(() => '<i class="fa fa-star"></i>')
              .join('')}
          </span>
          <span class="ms-2 text-muted">(${product.reviews} reviews)</span>
        </div>
        <p class="price mb-3">
          <span class="h4 text-primary">${campusMarketplace.formatCurrency(product.price)}</span>
          ${
            product.originalPrice
              ? `<del class="ms-2">${campusMarketplace.formatCurrency(product.originalPrice)}</del>`
              : ''
          }
        </p>
        <p>${product.description}</p>
        <div class="d-flex gap-2">
          <button class="btn btn-primary" onclick="addToCart(${product.id}, '${product.name}', ${product.price}, '${product.image}')">
            <i class="fa fa-shopping-cart"></i> Add to Cart
          </button>
          <button class="btn btn-outline-dark">
            <i class="fa fa-heart"></i> Wishlist
          </button>
        </div>
      </div>
    </div>
  `;

  const modalBody = modal.querySelector('.modal-body');
  if (modalBody) {
    modalBody.innerHTML = content;
  }

  campusMarketplace.openModal('quickViewModal');
}

// Product Search
function searchProducts(query) {
  const products = document.querySelectorAll('.product-item');
  const queryLower = query.toLowerCase();

  products.forEach(product => {
    const name = (product.dataset.name || '').toLowerCase();
    const description = (product.textContent || '').toLowerCase();

    const matches = name.includes(queryLower) || description.includes(queryLower);
    product.style.display = matches ? 'block' : 'none';
  });

  updateProductCount();
}

// Update Product Count
function updateProductCount() {
  const visibleProducts = document.querySelectorAll('.product-item:not([style*="display: none"])');
  const countElement = document.getElementById('product-count');

  if (countElement) {
    countElement.textContent = `Showing ${visibleProducts.length} products`;
  }
}

// Price Range Filter
function filterByPriceRange(minPrice, maxPrice) {
  const products = document.querySelectorAll('.product-item');

  products.forEach(product => {
    const price = parseFloat(product.dataset.price || 0);
    product.style.display = price >= minPrice && price <= maxPrice ? 'block' : 'none';
  });

  updateProductCount();
}

// Rating Filter
function filterByRating(minRating) {
  const products = document.querySelectorAll('.product-item');

  products.forEach(product => {
    const rating = parseFloat(product.dataset.rating || 0);
    product.style.display = rating >= minRating ? 'block' : 'none';
  });

  updateProductCount();
}

// Add to Wishlist
function toggleWishlist(productId) {
  let wishlist = JSON.parse(localStorage.getItem('wishlist')) || [];

  if (wishlist.includes(productId)) {
    wishlist = wishlist.filter(id => id !== productId);
    campusMarketplace.showToast('Removed from wishlist', 'info');
  } else {
    wishlist.push(productId);
    campusMarketplace.showToast('Added to wishlist', 'success');
  }

  localStorage.setItem('wishlist', JSON.stringify(wishlist));
  updateWishlistUI();
}

// Update Wishlist UI
function updateWishlistUI() {
  const wishlist = JSON.parse(localStorage.getItem('wishlist')) || [];
  document.querySelectorAll('.wishlist-btn').forEach(btn => {
    const productId = btn.dataset.productId;
    btn.classList.toggle('active', wishlist.includes(parseInt(productId)));
  });
}

// Get Wishlist Count
function getWishlistCount() {
  const wishlist = JSON.parse(localStorage.getItem('wishlist')) || [];
  return wishlist.length;
}

// Initialize Product Page
document.addEventListener('DOMContentLoaded', function() {
  // Filter buttons
  document.querySelectorAll('[data-filter]').forEach(btn => {
    btn.addEventListener('click', function() {
      const filter = this.dataset.filter;
      filterProducts(filter);

      // Update active state
      document.querySelectorAll('[data-filter]').forEach(b => b.classList.remove('active'));
      this.classList.add('active');
    });
  });

  // Sort dropdown
  const sortSelect = document.getElementById('sort-select');
  if (sortSelect) {
    sortSelect.addEventListener('change', function() {
      filterProducts(null, this.value);
    });
  }

  // Search input (with debounce)
  const searchInput = document.getElementById('product-search');
  if (searchInput) {
    searchInput.addEventListener('input', campusMarketplace.debounce(function() {
      searchProducts(this.value);
    }, 300));
  }

  // Initialize image gallery
  const galleries = document.querySelectorAll('[data-gallery]');
  galleries.forEach(gallery => {
    new ImageGallery(
      gallery.querySelector('[data-main-image]'),
      gallery.querySelectorAll('[data-thumbnail]')
    );
  });

  // Wishlist buttons
  document.querySelectorAll('.wishlist-btn').forEach(btn => {
    btn.addEventListener('click', function() {
      toggleWishlist(this.dataset.productId);
    });
  });

  updateWishlistUI();

  // Price range filter
  const priceSlider = document.getElementById('price-range');
  if (priceSlider) {
    priceSlider.addEventListener('input', function() {
      filterByPriceRange(0, this.value);
    });
  }
});

// Export functions
window.productModule = {
  filterProducts,
  sortProductsBy,
  ImageGallery,
  openQuickView,
  searchProducts,
  filterByPriceRange,
  filterByRating,
  toggleWishlist,
  getWishlistCount,
};
