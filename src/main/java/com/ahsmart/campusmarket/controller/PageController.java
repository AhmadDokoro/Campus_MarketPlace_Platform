package com.ahsmart.campusmarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/product/product-list.html")
    public String productList() {
        return "templates/product/product-list";
    }

    @GetMapping("/buyer/orders.html")
    public String buyerOrders() {
        return "templates/buyer/orders";
    }

    @GetMapping("/cart/cart.html")
    public String cart() {
        return "templates/cart/cart";
    }

    @GetMapping("/auth/login.html")
    public String login() {
        return "templates/auth/login";
    }

    @GetMapping("/auth/register.html")
    public String register() {
        return "templates/auth/register";
    }

    @GetMapping("/product/add.html")
    public String productAdd() {
        return "templates/product/add";
    }

    @GetMapping("/seller/dashboard.html")
    public String sellerDashboard() {
        return "templates/seller/dashboard";
    }

    @GetMapping("/seller/sales-history.html")
    public String sellerSales() {
        return "templates/seller/sales-history";
    }

    @GetMapping("/admin/flagged-products.html")
    public String adminFlagged() {
        return "templates/admin/flagged-products";
    }

    @GetMapping("/product/detail.html")
    public String productDetail() {
        return "detail";
    }

    @GetMapping("/admin/sellerVerificationPage")
    public String verifyPage() {
        return "templates/admin/admin-seller-verification";
    }

    @GetMapping("/admin/flaggedProducts")
    public String flagged() {
        return "templates/admin/flagged-products";
    }
}
