package com.ahsmart.campusmarket.service.order;

import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Condition;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import com.ahsmart.campusmarket.model.enums.PaymentMethod;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import com.ahsmart.campusmarket.repositories.ChatRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.service.cart.CartService;
import com.ahsmart.campusmarket.service.chat.ChatSchemaCompatibilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MultiItemCashOnDeliveryCheckoutTests {

    private static final String LEGACY_CHAT_ORDER_UNIQUE_INDEX = "legacy_uq_chats_order_id";

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ChatSchemaCompatibilityService chatSchemaCompatibilityService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void legacyUniqueChatOrderConstraintBreaksMultiItemCashOnDeliveryCheckout() {
        CheckoutFixture fixture = createCheckoutFixture("legacy-break");
        createLegacyUniqueChatOrderIndex();

        try {
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    orderService.createOrderFromCart(fixture.buyer().getUserId(), PaymentMethod.CASH_ON_DELIVERY));

            String message = collectMessages(exception).toLowerCase();
            assertTrue(message.contains("constraint") || message.contains("unique") || message.contains("index"));
        } finally {
            dropLegacyUniqueChatOrderIndex();
        }
    }

    @Test
    void schemaCompatibilityFixAllowsMultiItemCashOnDeliveryCheckoutWithPerItemChats() {
        CheckoutFixture fixture = createCheckoutFixture("legacy-fix");
        createLegacyUniqueChatOrderIndex();

        chatSchemaCompatibilityService.removeLegacyUniqueOrderIdIndex();

        Order order = orderService.createOrderFromCart(fixture.buyer().getUserId(), PaymentMethod.CASH_ON_DELIVERY);

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(2, order.getOrderItems().size());
        order.getOrderItems().forEach(item ->
                assertTrue(chatRepository.findByOrderItemId(item.getOrderItemId()).isPresent()));

        dropLegacyUniqueChatOrderIndex();
    }

    private CheckoutFixture createCheckoutFixture(String suffix) {
        Users sellerUser = new Users();
        sellerUser.setFirstName("Seller");
        sellerUser.setLastName("User");
        sellerUser.setEmail("seller-" + suffix + "@umt.edu");
        sellerUser.setPassword("pass");
        sellerUser.setAcademicId("SELL-" + suffix);
        sellerUser.setRole(Role.SELLER);
        sellerUser.setCreatedAt(LocalDateTime.now());
        sellerUser = usersRepository.save(sellerUser);

        Seller seller = new Seller();
        seller.setUser(sellerUser);
        seller.setIdCardImageUrl("http://example.com/id.png");
        seller.setMynemoProfileUrl("http://example.com/mynemo.png");
        seller.setStatus(SellerStatus.APPROVED);
        seller = sellerRepository.save(seller);

        Category category = new Category();
        category.setCategoryName("Checkout " + suffix);
        category.setDescription("Checkout fixture");
        category = categoryRepository.save(category);

        Product productOne = new Product();
        productOne.setSeller(seller);
        productOne.setCategory(category);
        productOne.setTitle("Product One " + suffix);
        productOne.setDescription("First product");
        productOne.setPrice(new BigDecimal("12.50"));
        productOne.setQuantity(10);
        productOne.setCondition(Condition.NEW);
        productOne.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        productOne = productRepository.save(productOne);

        Product productTwo = new Product();
        productTwo.setSeller(seller);
        productTwo.setCategory(category);
        productTwo.setTitle("Product Two " + suffix);
        productTwo.setDescription("Second product");
        productTwo.setPrice(new BigDecimal("20.00"));
        productTwo.setQuantity(10);
        productTwo.setCondition(Condition.USED);
        productTwo.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        productTwo = productRepository.save(productTwo);

        Users buyer = new Users();
        buyer.setFirstName("Buyer");
        buyer.setLastName("User");
        buyer.setEmail("buyer-" + suffix + "@umt.edu");
        buyer.setPassword("pass");
        buyer.setAcademicId("BUY-" + suffix);
        buyer.setRole(Role.BUYER);
        buyer.setCreatedAt(LocalDateTime.now());
        buyer = usersRepository.save(buyer);

        cartService.addToCart(buyer.getUserId(), productOne.getProductId(), 1);
        cartService.addToCart(buyer.getUserId(), productTwo.getProductId(), 1);

        return new CheckoutFixture(buyer);
    }

    private void createLegacyUniqueChatOrderIndex() {
        dropLegacyUniqueChatOrderIndex();
        jdbcTemplate.execute("CREATE UNIQUE INDEX " + LEGACY_CHAT_ORDER_UNIQUE_INDEX + " ON chats(order_id)");
    }

    private void dropLegacyUniqueChatOrderIndex() {
        jdbcTemplate.execute("DROP INDEX IF EXISTS " + LEGACY_CHAT_ORDER_UNIQUE_INDEX);
    }

    private String collectMessages(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null) {
                builder.append(current.getMessage()).append(' ');
            }
            current = current.getCause();
        }
        return builder.toString();
    }

    private record CheckoutFixture(Users buyer) {
    }
}
