package com.ahsmart.campusmarket.service.chat;

import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.model.Chat;
import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.OrderItem;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Condition;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import com.ahsmart.campusmarket.model.enums.PaymentStatus;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.payloadDTOs.chat.MessageDTO;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import com.ahsmart.campusmarket.repositories.ChatRepository;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.OrderRepository;
import com.ahsmart.campusmarket.repositories.PaymentRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import com.ahsmart.campusmarket.service.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ChatServiceIntegrationTests {

    @Autowired
    ChatService chatService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    ChatRepository chatRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    SellerRepository sellerRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Test
    void paymentSuccessCreatesChatPerOrderItem_andParticipantsCanExchangeMessages() {
        Users sellerUser = new Users();
        sellerUser.setFirstName("Sara");
        sellerUser.setLastName("Seller");
        sellerUser.setEmail("chat-seller@umt.edu");
        sellerUser.setPassword("pass");
        sellerUser.setAcademicId("CHAT-SELLER-1");
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
        category.setCategoryName("Chat Category");
        category.setDescription("Chat integration tests");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle("Desk Fan");
        product.setDescription("Quiet fan");
        product.setPrice(new BigDecimal("49.90"));
        product.setQuantity(5);
        product.setCondition(Condition.NEW);
        product.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        product = productRepository.save(product);

        Users buyer = new Users();
        buyer.setFirstName("Bashir");
        buyer.setLastName("Buyer");
        buyer.setEmail("chat-buyer@umt.edu");
        buyer.setPassword("pass");
        buyer.setAcademicId("CHAT-BUYER-1");
        buyer.setRole(Role.BUYER);
        buyer.setCreatedAt(LocalDateTime.now());
        buyer = usersRepository.save(buyer);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setTotalAmount(new BigDecimal("49.90"));
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        order = orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setSeller(seller);
        orderItem.setUnitPrice(new BigDecimal("49.90"));
        orderItem.setQuantity(1);
        orderItem.setSubtotal(new BigDecimal("49.90"));
        orderItem.setDeliveryStatus(DeliveryStatus.PENDING);
        orderItem = orderItemRepository.save(orderItem);

        paymentService.createPendingPayment(order);
        Map<String, Object> paymentResult = paymentService.processPayment(order.getOrderId(), "4242424242424242");

        assertTrue(Boolean.TRUE.equals(paymentResult.get("success")));
        assertEquals(PaymentStatus.SUCCESS, paymentRepository.findByOrder_OrderId(order.getOrderId()).orElseThrow().getStatus());

        Chat chat = chatRepository.findByOrderItemId(orderItem.getOrderItemId()).orElseThrow();
        assertNotNull(chat.getChatId());
        assertEquals(orderItem.getOrderItemId(), chat.getOrderItem().getOrderItemId());

        MessageDTO savedMessage = chatService.sendMessage(chat.getChatId(), buyer.getUserId(), "Hello seller, where are you now?");
        assertEquals(chat.getChatId(), savedMessage.getChatId());
        assertEquals(buyer.getUserId(), savedMessage.getSenderId());

        List<MessageDTO> messages = chatService.getMessages(chat.getChatId(), sellerUser.getUserId());
        assertEquals(1, messages.size());
        assertEquals("Hello seller, where are you now?", messages.get(0).getMessage());
    }

    @Test
    void outsiderCannotReadOrSendMessagesForAnotherOrderItemChat() {
        Users sellerUser = new Users();
        sellerUser.setFirstName("Ola");
        sellerUser.setLastName("Seller");
        sellerUser.setEmail("chat-seller-2@umt.edu");
        sellerUser.setPassword("pass");
        sellerUser.setAcademicId("CHAT-SELLER-2");
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
        category.setCategoryName("Chat Security Category");
        category.setDescription("Chat security tests");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle("Portable Speaker");
        product.setDescription("Speaker");
        product.setPrice(new BigDecimal("60.00"));
        product.setQuantity(5);
        product.setCondition(Condition.NEW);
        product.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        product = productRepository.save(product);

        Users buyer = new Users();
        buyer.setFirstName("Ivy");
        buyer.setLastName("Buyer");
        buyer.setEmail("chat-buyer-2@umt.edu");
        buyer.setPassword("pass");
        buyer.setAcademicId("CHAT-BUYER-2");
        buyer.setRole(Role.BUYER);
        buyer.setCreatedAt(LocalDateTime.now());
        buyer = usersRepository.save(buyer);

        Users outsider = new Users();
        outsider.setFirstName("Omar");
        outsider.setLastName("Outsider");
        outsider.setEmail("chat-outsider@umt.edu");
        outsider.setPassword("pass");
        outsider.setAcademicId("CHAT-OUTSIDER-1");
        outsider.setRole(Role.BUYER);
        outsider.setCreatedAt(LocalDateTime.now());
        outsider = usersRepository.save(outsider);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setTotalAmount(new BigDecimal("60.00"));
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        order = orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setSeller(seller);
        orderItem.setUnitPrice(new BigDecimal("60.00"));
        orderItem.setQuantity(1);
        orderItem.setSubtotal(new BigDecimal("60.00"));
        orderItem.setDeliveryStatus(DeliveryStatus.PENDING);
        orderItem = orderItemRepository.save(orderItem);

        paymentService.createPendingPayment(order);
        paymentService.processPayment(order.getOrderId(), "4242424242424242");

        Chat chat = chatRepository.findByOrderItemId(orderItem.getOrderItemId()).orElseThrow();
        Long chatId = chat.getChatId();
        Long outsiderUserId = outsider.getUserId();

        assertThrows(SecurityException.class, () -> chatService.getMessages(chatId, outsiderUserId));
        assertThrows(SecurityException.class, () -> chatService.sendMessage(chatId, outsiderUserId, "I should not be here"));
    }

    @Test
    void sendMessageRejectsBlankAndTooLongMessages() {
        Users sellerUser = new Users();
        sellerUser.setFirstName("Musa");
        sellerUser.setLastName("Seller");
        sellerUser.setEmail("chat-seller-3@umt.edu");
        sellerUser.setPassword("pass");
        sellerUser.setAcademicId("CHAT-SELLER-3");
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
        category.setCategoryName("Chat Validation Category");
        category.setDescription("Chat validation tests");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle("Campus Router");
        product.setDescription("Router");
        product.setPrice(new BigDecimal("75.00"));
        product.setQuantity(5);
        product.setCondition(Condition.NEW);
        product.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        product = productRepository.save(product);

        Users buyer = new Users();
        buyer.setFirstName("Nia");
        buyer.setLastName("Buyer");
        buyer.setEmail("chat-buyer-3@umt.edu");
        buyer.setPassword("pass");
        buyer.setAcademicId("CHAT-BUYER-3");
        buyer.setRole(Role.BUYER);
        buyer.setCreatedAt(LocalDateTime.now());
        buyer = usersRepository.save(buyer);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setTotalAmount(new BigDecimal("75.00"));
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        order = orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setSeller(seller);
        orderItem.setUnitPrice(new BigDecimal("75.00"));
        orderItem.setQuantity(1);
        orderItem.setSubtotal(new BigDecimal("75.00"));
        orderItem.setDeliveryStatus(DeliveryStatus.PENDING);
        orderItem = orderItemRepository.save(orderItem);

        paymentService.createPendingPayment(order);
        paymentService.processPayment(order.getOrderId(), "4242424242424242");

        Chat chat = chatRepository.findByOrderItemId(orderItem.getOrderItemId()).orElseThrow();
        Long chatId = chat.getChatId();
        Long buyerUserId = buyer.getUserId();
        String tooLongMessage = "x".repeat(1001);

        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(chatId, buyerUserId, "   "));
        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(chatId, buyerUserId, tooLongMessage));
    }
}
