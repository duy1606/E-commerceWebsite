package com.example.orderservice.dto.request;

import com.example.orderservice.entity.OrderDetail;
import com.example.orderservice.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    LocalDateTime dateCreated;
    PaymentStatus payment;
    double totalPrice;
    String address;
    String customerID;
    String voucherID;
    List<OrderDetail> orderDetails;
}
