package com.example.orderservice.enums;

public enum OrderStatus {
    ORDERED("ORDERED"),
    CANCELED("CANCELED"),
    PROCESSING("PROCESSING");
    OrderStatus(String name) {
        this.name = name;
    }

    private final String name;
}
