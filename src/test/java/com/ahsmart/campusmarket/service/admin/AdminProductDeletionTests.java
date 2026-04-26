package com.ahsmart.campusmarket.service.admin;

import com.ahsmart.campusmarket.helper.EmailHelper;
import com.ahsmart.campusmarket.model.Cart;
import com.ahsmart.campusmarket.model.CartItem;
import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.OrderItem;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.ProductImage;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Condition;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.repositories.CartItemRepository;
import com.ahsmart.campusmarket.repositories.CartRepository;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.OrderRepository;
import com.ahsmart.campusmarket.repositories.ProductImageRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.service.product.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminProductDeletionTests {

    @Autowired
    AdminService adminService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    SellerRepository sellerRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @MockBean
    EmailHelper emailHelper;

    @MockBean
    FileService fileService;

    @Test
    void adminDeleteProduct_removesCartRowsAndDeletesProduct() {
        Product product = createProduct("Flagged chair");

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1/flagged-chair.jpg");
        image.setIsPrimary(true);
        productImageRepository.save(image);

        Users buyer = createUser("buyer.one@umt.edu", Role.BUYER, "BUY-1");
        Cart cart = new Cart();
        cart.setUser(buyer);
        cart = cartRepository.save(cart);

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItemRepository.save(cartItem);

        adminService.adminDeleteProduct(product.getProductId());

        assertFalse(productRepository.existsById(product.getProductId()));
        assertFalse(cartItemRepository.existsByProduct_ProductId(product.getProductId()));
        assertTrue(productImageRepository.findByProduct_ProductId(product.getProductId()).isEmpty());
        verify(fileService, times(1)).deleteImageByUrl("https://res.cloudinary.com/demo/image/upload/v1/flagged-chair.jpg");
    }

    @Test
    void adminDeleteProduct_rejectsProductsWithOrderHistory() {
        Product product = createProduct("Flagged laptop");
        Users buyer = createUser("buyer.two@umt.edu", Role.BUYER, "BUY-2");

        Order order = new Order();
        order.setBuyer(buyer);
        order.setTotalAmount(BigDecimal.valueOf(1200));
        order.setStatus(OrderStatus.PAID);
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        order = orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setSeller(product.getSeller());
        orderItem.setUnitPrice(product.getPrice());
        orderItem.setQuantity(1);
        orderItem.setSubtotal(product.getPrice());
        orderItem.setDeliveryStatus(DeliveryStatus.PENDING);
        orderItemRepository.save(orderItem);

        assertThrows(IllegalArgumentException.class, () -> adminService.adminDeleteProduct(product.getProductId()));

        assertTrue(productRepository.existsById(product.getProductId()));
        assertTrue(orderItemRepository.existsByProduct_ProductId(product.getProductId()));
    }

    private Product createProduct(String title) {
        String normalized = title.replace(' ', '.').toLowerCase();
        Users sellerUser = createUser(normalized + "@umt.edu", Role.SELLER, normalized.substring(0, 3).toUpperCase() + "-SELL");

        Seller seller = new Seller();
        seller.setUser(sellerUser);
        seller.setIdCardImageUrl("https://example.com/id-card.png");
        seller.setMynemoProfileUrl("https://example.com/profile");
        seller.setStatus(SellerStatus.APPROVED);
        seller = sellerRepository.save(seller);

        Category category = new Category();
        category.setCategoryName(title + " Category");
        category.setDescription("Flagged listing category");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle(title);
        product.setDescription("Suspicious product for delete flow test");
        product.setPrice(BigDecimal.valueOf(25));
        product.setQuantity(1);
        product.setCondition(Condition.NEW);
        product.setFlaggedStatus(FlaggedStatus.SUSPICIOUS);
        return productRepository.save(product);
    }

    private Users createUser(String email, Role role, String academicId) {
        Users user = new Users();
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAcademicId(academicId);
        user.setRole(role);
        return usersRepository.save(user);
    }
}
