package com.example.orderservice.enums;

public enum PaymentStatus {
    DIRECT_PAYMENT("Direct Payment"),
    PAY_WITH_VNPAY("Pay With VNPAY")
    ;

    PaymentStatus(String name) {
        this.name = name;
    }

    private final String name;
}
