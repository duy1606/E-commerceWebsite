package com.example.orderservice.enums;

public enum OrderStatus {
    ORDERED("Ordered"),
    CANCELED("Canceled")
    ;

    OrderStatus(String name) {
        this.name = name;
    }

    private final String name;
}
