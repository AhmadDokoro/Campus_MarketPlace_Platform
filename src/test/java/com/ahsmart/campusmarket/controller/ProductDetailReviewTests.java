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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductDetailReviewTests {

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
    void productDetail_showsProductSpecificRatingAndReviews_notSellerWideData() throws Exception {
        Users firstBuyer = saveBuyer("detail-buyer-1@umt.edu", "BUY-DETAIL-1");
        Users secondBuyer = saveBuyer("detail-buyer-2@umt.edu", "BUY-DETAIL-2");
        Seller seller = saveSeller("detail-seller@umt.edu", "SELL-DETAIL");
        Category category = saveCategory("Detail Category");

        Product focusedProduct = saveProduct(seller, category, "Focused Product", "21.00");
        Product otherProduct = saveProduct(seller, category, "Other Seller Product", "25.00");

        Order firstFocusedOrder = saveOrder(firstBuyer, "21.00");
        OrderItem firstFocusedItem = saveOrderItem(firstFocusedOrder, focusedProduct, seller, "21.00");
        saveReview(firstBuyer, seller, firstFocusedOrder, firstFocusedItem, 5, "Focused review one");

        Order secondFocusedOrder = saveOrder(secondBuyer, "21.00");
        OrderItem secondFocusedItem = saveOrderItem(secondFocusedOrder, focusedProduct, seller, "21.00");
        saveReview(secondBuyer, seller, secondFocusedOrder, secondFocusedItem, 4, "Focused review two");

        Order otherProductOrder = saveOrder(firstBuyer, "25.00");
        OrderItem otherProductItem = saveOrderItem(otherProductOrder, otherProduct, seller, "25.00");
        saveReview(firstBuyer, seller, otherProductOrder, otherProductItem, 1, "Other product review");

        mockMvc.perform(get("/products/{productId}", focusedProduct.getProductId()))
                .andExpect(status().isOk())
                .andExpect(view().name("product-listings/product-detail"))
                .andExpect(content().string(containsString("4.5 / 5 (2 reviews)")))
                .andExpect(content().string(containsString("Focused review one")))
                .andExpect(content().string(containsString("Focused review two")))
                .andExpect(content().string(containsString("View all reviews")))
                .andExpect(content().string(not(containsString("Other product review"))));
    }

    private Users saveBuyer(String email, String academicId) {
        Users buyer = new Users();
        buyer.setFirstName("Buyer");
        buyer.setLastName("Detail");
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
        sellerUser.setLastName("Detail");
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
        category.setDescription("Product detail test category");
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

    private Order saveOrder(Users buyer, String totalAmount) {
        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PAID);
        order.setDeliveryStatus(DeliveryStatus.PENDING);
        order.setTotalAmount(new BigDecimal(totalAmount));
        return orderRepository.save(order);
    }

    private OrderItem saveOrderItem(Order order, Product product, Seller seller, String unitPrice) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setSeller(seller);
        item.setUnitPrice(new BigDecimal(unitPrice));
        item.setQuantity(1);
        item.setSubtotal(new BigDecimal(unitPrice));
        item.setDeliveryStatus(DeliveryStatus.RECEIVED);
        return orderItemRepository.save(item);
    }

    private void saveReview(Users buyer, Seller seller, Order order, OrderItem orderItem, int rating, String comment) {
        Review review = new Review();
        review.setReviewer(buyer);
        review.setTargetSeller(seller.getUser());
        review.setOrder(order);
        review.setOrderItem(orderItem);
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);
    }
}
