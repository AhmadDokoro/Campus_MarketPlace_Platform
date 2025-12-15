/* ==================== GLOBAL FUNCTIONS ==================== */

// Toast Notification
function showToast(message, type = 'success') {
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 3000);
}

// Format Currency
function formatCurrency(amount) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
}

// Navbar Toggle
document.addEventListener('DOMContentLoaded', function() {
  const navbarToggler = document.querySelector('.navbar-toggler');
  const navbarCollapse = document.querySelector('#navbarCollapse');

  if (navbarToggler) {
    navbarToggler.addEventListener('click', function() {
      navbarCollapse?.classList.toggle('show');
    });
  }

  // Close navbar on link click
  const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
  navLinks.forEach(link => {
    link.addEventListener('click', function() {
      navbarCollapse?.classList.remove('show');
    });
  });

  // Close navbar when clicking outside
  document.addEventListener('click', function(event) {
    if (!event.target.closest('.navbar')) {
      navbarCollapse?.classList.remove('show');
    }
  });
});

// Dropdown Toggle
document.addEventListener('DOMContentLoaded', function() {
  const dropdownToggles = document.querySelectorAll('[data-toggle="dropdown"]');

  dropdownToggles.forEach(toggle => {
    toggle.addEventListener('click', function(e) {
      e.preventDefault();
      const target = document.querySelector(this.getAttribute('href'));
      if (target) {
        target.classList.toggle('show');
      }
    });
  });

  // Close dropdown when clicking outside
  document.addEventListener('click', function(event) {
    if (!event.target.closest('[data-toggle="dropdown"]')) {
      document.querySelectorAll('.collapse.show').forEach(el => {
        el.classList.remove('show');
      });
    }
  });
});

// Back to Top Button
document.addEventListener('DOMContentLoaded', function() {
  const backToTopBtn = document.querySelector('.back-to-top');

  if (backToTopBtn) {
    window.addEventListener('scroll', function() {
      if (window.pageYOffset > 100) {
        backToTopBtn.style.display = 'block';
      } else {
        backToTopBtn.style.display = 'none';
      }
    });

    backToTopBtn.addEventListener('click', function(e) {
      e.preventDefault();
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      });
    });
  }
});

// Smooth Scroll
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
  anchor.addEventListener('click', function(e) {
    const href = this.getAttribute('href');
    if (href !== '#' && document.querySelector(href)) {
      e.preventDefault();
      document.querySelector(href).scrollIntoView({
        behavior: 'smooth',
      });
    }
  });
});

// Modal Functions
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('show');
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('show');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';
  }
}

// Close modal on background click
document.addEventListener('click', function(event) {
  if (event.target.classList.contains('modal')) {
    closeModal(event.target.id);
  }
});

// Modal close button
document.querySelectorAll('.modal-close').forEach(btn => {
  btn.addEventListener('click', function() {
    const modal = this.closest('.modal');
    if (modal) {
      closeModal(modal.id);
    }
  });
});

// Image Gallery Zoom
document.addEventListener('DOMContentLoaded', function() {
  const productImages = document.querySelectorAll('.product-img img');

  productImages.forEach(img => {
    img.addEventListener('click', function() {
      if (this.style.cursor === 'zoom-in') {
        this.style.cursor = 'zoom-out';
        this.style.transform = 'scale(2)';
      } else {
        this.style.cursor = 'zoom-in';
        this.style.transform = 'scale(1)';
      }
    });
  });
});

// Form Validation
function validateForm(formId) {
  const form = document.getElementById(formId);
  if (!form) return false;

  const inputs = form.querySelectorAll('[required]');
  let isValid = true;

  inputs.forEach(input => {
    if (!input.value.trim()) {
      input.classList.add('is-invalid');
      isValid = false;
    } else {
      input.classList.remove('is-invalid');
    }
  });

  return isValid;
}

// Add event listeners to required inputs
document.addEventListener('DOMContentLoaded', function() {
  const requiredInputs = document.querySelectorAll('[required]');

  requiredInputs.forEach(input => {
    input.addEventListener('blur', function() {
      if (!this.value.trim()) {
        this.classList.add('is-invalid');
      } else {
        this.classList.remove('is-invalid');
      }
    });

    input.addEventListener('input', function() {
      if (this.value.trim()) {
        this.classList.remove('is-invalid');
      }
    });
  });
});

// Lazy Load Images
if ('IntersectionObserver' in window) {
  const imageObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target;
        img.src = img.dataset.src || img.src;
        img.classList.add('loaded');
        observer.unobserve(img);
      }
    });
  });

  document.querySelectorAll('img[data-src]').forEach(img => {
    imageObserver.observe(img);
  });
}

// Number Input Controls
document.addEventListener('DOMContentLoaded', function() {
  const numberInputs = document.querySelectorAll('input[type="number"]');

  numberInputs.forEach(input => {
    // Prevent negative values
    input.addEventListener('change', function() {
      if (this.min && this.value < this.min) {
        this.value = this.min;
      }
      if (this.max && this.value > this.max) {
        this.value = this.max;
      }
    });
  });
});

// Utility: Get Query Parameters
function getQueryParam(param) {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get(param);
}

// Utility: Set Query Parameters
function setQueryParam(param, value) {
  const urlParams = new URLSearchParams(window.location.search);
  urlParams.set(param, value);
  window.history.replaceState({}, '', `?${urlParams}`);
}

// Utility: Debounce Function
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// Confirm Dialog
function showConfirm(message, onConfirm, onCancel) {
  if (confirm(message)) {
    onConfirm?.();
  } else {
    onCancel?.();
  }
}

// Export functions for use in other scripts
window.campusMarketplace = {
  showToast,
  formatCurrency,
  openModal,
  closeModal,
  validateForm,
  getQueryParam,
  setQueryParam,
  debounce,
  showConfirm,
};
