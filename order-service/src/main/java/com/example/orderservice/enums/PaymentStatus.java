package com.example.orderservice.enums;

public enum PaymentStatus {
    DIRECT_PAYMENT("DIRECT_PAYMENT"),
    PAY_WITH_VNPAY("PAY_WITH_VNPAY")
    ;

    PaymentStatus(String name) {
        this.name = name;
    }

    private final String name;
}
