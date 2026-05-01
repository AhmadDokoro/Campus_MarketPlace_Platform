package com.ahsmart.campusmarket.helper;

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
import com.ahsmart.campusmarket.payloadDTOs.review.ProductRatingData;
import com.ahsmart.campusmarket.repositories.CategoryRepository;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.OrderRepository;
import com.ahsmart.campusmarket.repositories.ProductRepository;
import com.ahsmart.campusmarket.repositories.ReviewRepository;
import com.ahsmart.campusmarket.repositories.SellerRepository;
import com.ahsmart.campusmarket.repositories.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ProductRatingHelperTests {

    @Autowired
    private ProductRatingHelper productRatingHelper;

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
    void getRatingData_returnsProductSpecificAggregate_whenSellerHasMultipleProducts() {
        Users buyer = saveBuyer("helper-buyer@umt.edu", "BUY-HELPER");
        Seller seller = saveSeller("helper-seller@umt.edu", "SELL-HELPER");
        Category category = saveCategory("Helper Category");

        Product reviewedProduct = saveProduct(seller, category, "Reviewed Product", "11.00");
        Product otherProduct = saveProduct(seller, category, "Other Product", "13.00");

        Order firstOrder = saveOrder(buyer, "11.00");
        OrderItem firstReviewedItem = saveOrderItem(firstOrder, reviewedProduct, seller, "11.00");
        saveReview(buyer, seller, firstOrder, firstReviewedItem, 5, "Excellent");

        Order secondOrder = saveOrder(buyer, "13.00");
        OrderItem otherProductItem = saveOrderItem(secondOrder, otherProduct, seller, "13.00");
        saveReview(buyer, seller, secondOrder, otherProductItem, 2, "Needs work");

        Order thirdOrder = saveOrder(buyer, "11.00");
        OrderItem secondReviewedItem = saveOrderItem(thirdOrder, reviewedProduct, seller, "11.00");
        saveReview(buyer, seller, thirdOrder, secondReviewedItem, 4, "Solid");

        ProductRatingData reviewedProductRating = productRatingHelper.getRatingData(reviewedProduct.getProductId());
        ProductRatingData otherProductRating = productRatingHelper.getRatingData(otherProduct.getProductId());

        assertEquals(2L, reviewedProductRating.reviewCount());
        assertEquals(4.5, reviewedProductRating.averageRating(), 0.001);
        assertEquals(1L, otherProductRating.reviewCount());
        assertEquals(2.0, otherProductRating.averageRating(), 0.001);
    }

    private Users saveBuyer(String email, String academicId) {
        Users buyer = new Users();
        buyer.setFirstName("Buyer");
        buyer.setLastName("Helper");
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
        sellerUser.setLastName("Helper");
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
        category.setDescription("Product rating helper test category");
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
