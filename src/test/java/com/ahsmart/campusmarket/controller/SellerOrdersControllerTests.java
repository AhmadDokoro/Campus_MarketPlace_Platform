package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.OrderItem;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.Seller;
import com.ahsmart.campusmarket.model.Users;
import com.ahsmart.campusmarket.model.enums.Condition;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.FlaggedStatus;
import com.ahsmart.campusmarket.model.enums.OrderStatus;
import com.ahsmart.campusmarket.model.enums.Role;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.OrderRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SellerOrdersControllerTests {

    @Autowired
    MockMvc mockMvc;

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

    @Test
    void sellerOrders_notLoggedIn_redirectsToSignin() throws Exception {
        mockMvc.perform(get("/seller/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"));
    }

    @Test
    void sellerOrders_showsOnlyPaidItemsForLoggedInSeller() throws Exception {
        Users sellerUser = new Users();
        sellerUser.setFirstName("Amina");
        sellerUser.setLastName("Seller");
        sellerUser.setEmail("seller-orders@umt.edu");
        sellerUser.setPassword("pass");
        sellerUser.setAcademicId("SELL-200");
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
        category.setCategoryName("Seller Orders Category");
        category.setDescription("Category for seller order tests");
        category = categoryRepository.save(category);

        Product paidProduct = new Product();
        paidProduct.setSeller(seller);
        paidProduct.setCategory(category);
        paidProduct.setTitle("Paid Notebook");
        paidProduct.setDescription("Paid item");
        paidProduct.setPrice(new BigDecimal("15.00"));
        paidProduct.setQuantity(10);
        paidProduct.setCondition(Condition.USED);
        paidProduct.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        paidProduct = productRepository.save(paidProduct);

        Product unpaidProduct = new Product();
        unpaidProduct.setSeller(seller);
        unpaidProduct.setCategory(category);
        unpaidProduct.setTitle("Unpaid Pen");
        unpaidProduct.setDescription("Unpaid item");
        unpaidProduct.setPrice(new BigDecimal("5.00"));
        unpaidProduct.setQuantity(10);
        unpaidProduct.setCondition(Condition.NEW);
        unpaidProduct.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        unpaidProduct = productRepository.save(unpaidProduct);

        Users buyer = new Users();
        buyer.setFirstName("Bilal");
        buyer.setLastName("Buyer");
        buyer.setEmail("buyer-orders@umt.edu");
        buyer.setPassword("pass");
        buyer.setAcademicId("BUY-200");
        buyer.setRole(Role.BUYER);
        buyer.setCreatedAt(LocalDateTime.now());
        buyer = usersRepository.save(buyer);

        Order paidOrder = new Order();
        paidOrder.setBuyer(buyer);
        paidOrder.setTotalAmount(new BigDecimal("15.00"));
        paidOrder.setStatus(OrderStatus.PAID);
        paidOrder.setDeliveryStatus(DeliveryStatus.PENDING);
        paidOrder = orderRepository.save(paidOrder);

        Order unpaidOrder = new Order();
        unpaidOrder.setBuyer(buyer);
        unpaidOrder.setTotalAmount(new BigDecimal("5.00"));
        unpaidOrder.setStatus(OrderStatus.PENDING_PAYMENT);
        unpaidOrder.setDeliveryStatus(DeliveryStatus.PENDING);
        unpaidOrder = orderRepository.save(unpaidOrder);

        OrderItem paidItem = new OrderItem();
        paidItem.setOrder(paidOrder);
        paidItem.setProduct(paidProduct);
        paidItem.setSeller(seller);
        paidItem.setUnitPrice(new BigDecimal("15.00"));
        paidItem.setQuantity(1);
        paidItem.setSubtotal(new BigDecimal("15.00"));
        paidItem.setDeliveryStatus(DeliveryStatus.IN_CAMPUS);
        orderItemRepository.save(paidItem);

        OrderItem unpaidItem = new OrderItem();
        unpaidItem.setOrder(unpaidOrder);
        unpaidItem.setProduct(unpaidProduct);
        unpaidItem.setSeller(seller);
        unpaidItem.setUnitPrice(new BigDecimal("5.00"));
        unpaidItem.setQuantity(1);
        unpaidItem.setSubtotal(new BigDecimal("5.00"));
        unpaidItem.setDeliveryStatus(DeliveryStatus.PENDING);
        orderItemRepository.save(unpaidItem);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", sellerUser.getUserId());

        mockMvc.perform(get("/seller/orders").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/orders"))
                .andExpect(content().string(containsString("Paid Notebook")))
                .andExpect(content().string(containsString("Bilal Buyer")))
                .andExpect(content().string(not(containsString("Unpaid Pen"))));
    }

    @Test
    void updateSellerOrderStatus_updatesOnlyOwnedOrderItemDeliveryStatus() throws Exception {
        Users sellerUser = new Users();
        sellerUser.setFirstName("Safa");
        sellerUser.setLastName("Seller");
        sellerUser.setEmail("seller-status@umt.edu");
        sellerUser.setPassword("pass");
        sellerUser.setAcademicId("SELL-201");
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
        category.setCategoryName("Seller Status Category");
        category.setDescription("Category for seller status tests");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle("Campus Lamp");
        product.setDescription("Lamp");
        product.setPrice(new BigDecimal("35.00"));
        product.setQuantity(4);
        product.setCondition(Condition.NEW);
        product.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        product = productRepository.save(product);

        Users buyer = new Users();
        buyer.setFirstName("Nora");
        buyer.setLastName("Buyer");
        buyer.setEmail("buyer-status@umt.edu");
        buyer.setPassword("pass");
        buyer.setAcademicId("BUY-201");
        buyer.setRole(Role.BUYER);
        buyer.setCreatedAt(LocalDateTime.now());
        buyer = usersRepository.save(buyer);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setTotalAmount(new BigDecimal("35.00"));
        order.setStatus(OrderStatus.PAID);
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        order = orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setSeller(seller);
        orderItem.setUnitPrice(new BigDecimal("35.00"));
        orderItem.setQuantity(1);
        orderItem.setSubtotal(new BigDecimal("35.00"));
        orderItem.setDeliveryStatus(DeliveryStatus.PENDING);
        orderItem = orderItemRepository.save(orderItem);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", sellerUser.getUserId());

        mockMvc.perform(post("/seller/orders/update-status")
                        .session(session)
                        .param("orderItemId", String.valueOf(orderItem.getOrderItemId()))
                        .param("newStatus", "IN_CAMPUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/orders"));

        OrderItem updated = orderItemRepository.findById(orderItem.getOrderItemId()).orElseThrow();
        assertEquals(DeliveryStatus.IN_CAMPUS, updated.getDeliveryStatus());
    }

    @Test
    void sellerOrders_loggedInBuyer_redirectsToStartSelling() throws Exception {
        Users buyerUser = new Users();
        buyerUser.setFirstName("Buyer");
        buyerUser.setLastName("Only");
        buyerUser.setEmail("buyer-only-orders@umt.edu");
        buyerUser.setPassword("pass");
        buyerUser.setAcademicId("BUY-202");
        buyerUser.setRole(Role.BUYER);
        buyerUser.setCreatedAt(LocalDateTime.now());
        buyerUser = usersRepository.save(buyerUser);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", buyerUser.getUserId());

        mockMvc.perform(get("/seller/orders").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/start-selling"));
    }

    @Test
    void updateSellerOrderStatus_rejectsInvalidTransitionAndKeepsCurrentStatus() throws Exception {
        Users sellerUser = new Users();
        sellerUser.setFirstName("Lina");
        sellerUser.setLastName("Seller");
        sellerUser.setEmail("seller-invalid-status@umt.edu");
        sellerUser.setPassword("pass");
        sellerUser.setAcademicId("SELL-202");
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
        category.setCategoryName("Seller Invalid Status Category");
        category.setDescription("Category for invalid status transition tests");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle("Campus Chair");
        product.setDescription("Chair");
        product.setPrice(new BigDecimal("20.00"));
        product.setQuantity(3);
        product.setCondition(Condition.NEW);
        product.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        product = productRepository.save(product);

        Users buyer = new Users();
        buyer.setFirstName("Mina");
        buyer.setLastName("Buyer");
        buyer.setEmail("buyer-invalid-status@umt.edu");
        buyer.setPassword("pass");
        buyer.setAcademicId("BUY-203");
        buyer.setRole(Role.BUYER);
        buyer.setCreatedAt(LocalDateTime.now());
        buyer = usersRepository.save(buyer);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setStatus(OrderStatus.PAID);
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        order = orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setSeller(seller);
        orderItem.setUnitPrice(new BigDecimal("20.00"));
        orderItem.setQuantity(1);
        orderItem.setSubtotal(new BigDecimal("20.00"));
        orderItem.setDeliveryStatus(DeliveryStatus.PENDING);
        orderItem = orderItemRepository.save(orderItem);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", sellerUser.getUserId());

        mockMvc.perform(post("/seller/orders/update-status")
                        .session(session)
                        .param("orderItemId", String.valueOf(orderItem.getOrderItemId()))
                        .param("newStatus", "DELIVERED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/orders"))
                .andExpect(flash().attributeExists("error"));

        OrderItem unchanged = orderItemRepository.findById(orderItem.getOrderItemId()).orElseThrow();
        assertEquals(DeliveryStatus.PENDING, unchanged.getDeliveryStatus());
    }
}
