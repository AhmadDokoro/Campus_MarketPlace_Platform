package com.ahsmart.campusmarket.model.enums;

// Order payment lifecycle status (matches DB ENUM).
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    CANCELLED,
    REFUNDED
}

