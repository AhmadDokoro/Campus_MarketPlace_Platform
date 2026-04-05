package com.ahsmart.campusmarket.controller;

import com.ahsmart.campusmarket.service.review.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Submits a seller review from the buyer's profile page.
    @PostMapping("/submit")
    public String submitReview(HttpSession session,
                               @RequestParam("orderItemId") Long orderItemId,
                               @RequestParam("rating") Integer rating,
                               @RequestParam(value = "comment", required = false) String comment,
                               RedirectAttributes redirectAttributes) {
        Long userId = resolveUserId(session);
        if (userId == null) return "redirect:/signin";

        try {
            reviewService.createReview(orderItemId, rating, comment, userId);
            redirectAttributes.addFlashAttribute("reviewSuccess", "Thank you for your review!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        }

        return "redirect:/user/profile";
    }

    private Long resolveUserId(HttpSession session) {
        Object obj = session.getAttribute("userId");
        if (obj == null) return null;
        return (obj instanceof Long l) ? l : Long.valueOf(String.valueOf(obj));
    }
}
