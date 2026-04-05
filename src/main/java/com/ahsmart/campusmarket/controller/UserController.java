package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.model.enums.DeliveryStatus;
import com.ahsmart.campusmarket.model.enums.SellerStatus;
import com.ahsmart.campusmarket.payloadDTOs.order.BuyerOrderItemChatDTO;
import com.ahsmart.campusmarket.payloadDTOs.order.BuyerOrderTrackingSummaryDTO;
import com.ahsmart.campusmarket.service.order.OrderService;
import com.ahsmart.campusmarket.service.review.ReviewService;
import com.ahsmart.campusmarket.service.user.StartSellingDecision;
import com.ahsmart.campusmarket.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final OrderService orderService;
    private final ReviewService reviewService;

    public UserController(UserService userService, OrderService orderService, ReviewService reviewService) {
        this.userService = userService;
        this.orderService = orderService;
        this.reviewService = reviewService;
    }

    // Displays the user profile page with order tracking counts.
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Long userId = resolveUserId(session);
        if (userId == null) return "redirect:/signin";

        BuyerOrderTrackingSummaryDTO trackingSummary = orderService.getBuyerTrackingSummary(userId);
        List<BuyerOrderItemChatDTO> buyerChatItems = orderService.getBuyerOrderItemsForChat(userId);

        long receivedCount = buyerChatItems.stream()
                .filter(item -> item.getDeliveryStatus() == DeliveryStatus.RECEIVED)
                .count();

        model.addAttribute("pendingPaymentCount", trackingSummary.getPendingPaymentCount());
        model.addAttribute("placedCount", trackingSummary.getPlacedCount());
        model.addAttribute("inCampusCount", trackingSummary.getInCampusCount());
        model.addAttribute("deliveredCount", trackingSummary.getDeliveredCount());
        model.addAttribute("receivedCount", receivedCount);
        model.addAttribute("buyerChatItems", buyerChatItems);
        model.addAttribute("reviewedOrderIds", reviewService.getReviewedOrderIds(userId));

        return "user/profile";
    }

    @GetMapping("/start-selling")
    public String startSelling(HttpSession session, Model model) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/signin";
        }

        // Session stores Long already in this project; still handle String just in case.
        Long userId;
        if (userIdObj instanceof Long l) {
            userId = l;
        } else {
            userId = Long.valueOf(String.valueOf(userIdObj));
        }

        StartSellingDecision decision;
        try {
            decision = userService.decideStartSelling(userId);
        } catch (IllegalArgumentException ex) {
            // user missing or invalid session → force re-login
            session.invalidate();
            return "redirect:/signin";
        }

        SellerStatus status = decision.sellerStatus();

        if (status == SellerStatus.APPROVED) {
            return "redirect:/seller/dashboard";
        }
        if (status == SellerStatus.PENDING) {
            return "seller/reviewPendingPage";
        }
        if (status == SellerStatus.REJECTED) {
            model.addAttribute("rejectionReason", decision.rejectionReason());
            return "seller/rejectApprove";
        }

        // No seller profile yet → allow request verification.
        session.setAttribute("pendingSellerUserId", userId);
        session.setAttribute("pendingSeller", true);
        return "redirect:/auth/requestVerification";
    }

    // Safely resolves user ID from session which may be Long or String.
    private Long resolveUserId(HttpSession session) {
        Object obj = session.getAttribute("userId");
        if (obj == null) return null;
        return (obj instanceof Long l) ? l : Long.valueOf(String.valueOf(obj));
    }
}
