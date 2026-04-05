package com.ahsmart.campusmarket.service.review;

import com.ahsmart.campusmarket.model.OrderItem;
import com.ahsmart.campusmarket.model.Review;
import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.repositories.OrderItemRepository;
import com.ahsmart.campusmarket.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public void createReview(Long orderItemId, Integer rating, String comment, Long buyerUserId) {
        if (orderItemId == null) {
            throw new IllegalArgumentException("Order item id is required.");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        if (buyerUserId == null) {
            throw new IllegalArgumentException("Buyer id is required.");
        }

        // Load order item with all required associations.
        OrderItem orderItem = orderItemRepository.findByIdWithOrderBuyerSellerAndUser(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found."));

        // Validate buyer owns this order.
        if (!orderItem.getOrder().getBuyer().getUserId().equals(buyerUserId)) {
            throw new IllegalArgumentException("You cannot review another buyer's order.");
        }

        // Validate item has been received by the buyer.
        if (orderItem.getDeliveryStatus() != DeliveryStatus.RECEIVED) {
            throw new IllegalArgumentException("You can only rate a seller after marking the item as received.");
        }

        // Prevent duplicate reviews (one review per order per buyer).
        Long orderId = orderItem.getOrder().getOrderId();
        if (reviewRepository.existsByOrder_OrderIdAndReviewer_UserId(orderId, buyerUserId)) {
            throw new IllegalArgumentException("You have already reviewed this order.");
        }

        Review review = new Review();
        review.setReviewer(orderItem.getOrder().getBuyer());
        review.setTargetSeller(orderItem.getSeller().getUser());
        review.setOrder(orderItem.getOrder());
        review.setRating(rating);
        review.setComment(comment == null ? "" : comment.trim());
        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getReviewedOrderIds(Long buyerUserId) {
        if (buyerUserId == null) {
            return Collections.emptySet();
        }
        return reviewRepository.findReviewedOrderIdsByReviewer(buyerUserId);
    }
}
