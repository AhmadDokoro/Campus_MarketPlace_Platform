package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.Category;
import com.ahsmart.campusmarket.model.Order;
import com.ahsmart.campusmarket.model.OrderItem;
import com.ahsmart.campusmarket.model.Product;
import com.ahsmart.campusmarket.model.Review;
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
import com.ahsmart.campusmarket.repositories.ReviewRepository;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserProfileOrderTrackingTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void profile_countsVisibleItemsPerStatus_andHidesReviewedReceivedItems() throws Exception {
        Users buyer = saveBuyer("profile-buyer@umt.edu", "BUY-PROFILE");
        Seller sellerOne = saveSeller("profile-seller-1@umt.edu", "SELL-PROFILE-1");
        Seller sellerTwo = saveSeller("profile-seller-2@umt.edu", "SELL-PROFILE-2");
        Category category = saveCategory("Profile Tracking Category");

        Product unpaidNotebook = saveProduct(sellerOne, category, "Unpaid Notebook", "10.00");
        Product unpaidLamp = saveProduct(sellerTwo, category, "Unpaid Lamp", "12.00");
        Product placedBottle = saveProduct(sellerOne, category, "Placed Bottle", "14.00");
        Product inCampusBag = saveProduct(sellerOne, category, "In Campus Bag", "16.00");
        Product deliveredChair = saveProduct(sellerTwo, category, "Delivered Chair", "18.00");
        Product reviewedMug = saveProduct(sellerOne, category, "Reviewed Mug", "20.00");
        Product awaitingReviewHeadset = saveProduct(sellerTwo, category, "Awaiting Review Headset", "22.00");

        Order pendingPaymentOrder = saveOrder(buyer, OrderStatus.PENDING_PAYMENT, "22.00");
        saveOrderItem(pendingPaymentOrder, unpaidNotebook, sellerOne, DeliveryStatus.PENDING, "10.00");
        saveOrderItem(pendingPaymentOrder, unpaidLamp, sellerTwo, DeliveryStatus.PENDING, "12.00");

        Order placedOrder = saveOrder(buyer, OrderStatus.PAID, "14.00");
        saveOrderItem(placedOrder, placedBottle, sellerOne, DeliveryStatus.PENDING, "14.00");

        Order inCampusOrder = saveOrder(buyer, OrderStatus.PAID, "16.00");
        saveOrderItem(inCampusOrder, inCampusBag, sellerOne, DeliveryStatus.IN_CAMPUS, "16.00");

        Order deliveredOrder = saveOrder(buyer, OrderStatus.PAID, "18.00");
        saveOrderItem(deliveredOrder, deliveredChair, sellerTwo, DeliveryStatus.DELIVERED, "18.00");

        Order receivedOrder = saveOrder(buyer, OrderStatus.PAID, "42.00");
        OrderItem reviewedItem = saveOrderItem(receivedOrder, reviewedMug, sellerOne, DeliveryStatus.RECEIVED, "20.00");
        OrderItem awaitingReviewItem = saveOrderItem(receivedOrder, awaitingReviewHeadset, sellerTwo, DeliveryStatus.RECEIVED, "22.00");

        Review review = new Review();
        review.setReviewer(buyer);
        review.setTargetSeller(sellerOne.getUser());
        review.setOrder(receivedOrder);
        review.setOrderItem(reviewedItem);
        review.setRating(5);
        review.setComment("Already reviewed.");
        reviewRepository.save(review);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", buyer.getUserId());
        session.setAttribute("userName", "Buyer Profile");
        session.setAttribute("userEmail", buyer.getEmail());

        mockMvc.perform(get("/user/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attribute("pendingPaymentCount", 2L))
                .andExpect(model().attribute("placedCount", 1))
                .andExpect(model().attribute("inCampusCount", 1))
                .andExpect(model().attribute("deliveredCount", 1))
                .andExpect(model().attribute("receivedCount", 1))
                .andExpect(model().attribute("placedItems", hasSize(1)))
                .andExpect(model().attribute("inCampusItems", hasSize(1)))
                .andExpect(model().attribute("deliveredItems", hasSize(1)))
                .andExpect(model().attribute("receivedItems", hasSize(1)))
                .andExpect(content().string(containsString("Awaiting Review Headset")))
                .andExpect(content().string(not(containsString("Reviewed Mug"))));
    }

    private Users saveBuyer(String email, String academicId) {
        Users buyer = new Users();
        buyer.setFirstName("Buyer");
        buyer.setLastName("Profile");
        buyer.setEmail(email);
        buyer.setPassword("pass");
        buyer.setAcademicId(academicId);
        buyer.setRole(Role.BUYER);
        buyer.setCreatedAt(LocalDateTime.now());
        return usersRepository.save(buyer);
    }

    private Seller saveSeller(String email, String academicId) {
        Users sellerUser = new Users();
        sellerUser.setFirstName("Seller");
        sellerUser.setLastName("Profile");
        sellerUser.setEmail(email);
        sellerUser.setPassword("pass");
        sellerUser.setAcademicId(academicId);
        sellerUser.setRole(Role.SELLER);
        sellerUser.setCreatedAt(LocalDateTime.now());
        sellerUser = usersRepository.save(sellerUser);

        Seller seller = new Seller();
        seller.setUser(sellerUser);
        seller.setIdCardImageUrl("http://example.com/id.png");
        seller.setMynemoProfileUrl("http://example.com/mynemo.png");
        seller.setStatus(SellerStatus.APPROVED);
        return sellerRepository.save(seller);
    }

    private Category saveCategory(String name) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setDescription("Profile tracking test category");
        return categoryRepository.save(category);
    }

    private Product saveProduct(Seller seller, Category category, String title, String price) {
        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle(title);
        product.setDescription(title + " description");
        product.setPrice(new BigDecimal(price));
        product.setQuantity(10);
        product.setCondition(Condition.NEW);
        product.setFlaggedStatus(FlaggedStatus.UNKNOWN);
        return productRepository.save(product);
    }

    private Order saveOrder(Users buyer, OrderStatus status, String totalAmount) {
        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(status);
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        order.setTotalAmount(new BigDecimal(totalAmount));
        return orderRepository.save(order);
    }

    private OrderItem saveOrderItem(Order order,
                                    Product product,
                                    Seller seller,
                                    DeliveryStatus deliveryStatus,
                                    String unitPrice) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setSeller(seller);
        item.setUnitPrice(new BigDecimal(unitPrice));
        item.setQuantity(1);
        item.setSubtotal(new BigDecimal(unitPrice));
        item.setDeliveryStatus(deliveryStatus);
        return orderItemRepository.save(item);
    }
}
