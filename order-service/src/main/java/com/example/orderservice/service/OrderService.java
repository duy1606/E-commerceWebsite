package com.example.orderservice.service;

import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.OrderResponse;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    OrderMapper orderMapper;

    public ApiResponse<List<OrderResponse>> findByDateOrder(LocalDate date){
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23,59,59);

        var listOrder = orderRepository.findByDateCreatedBetween(startOfDay, endOfDay);

        return ApiResponse.<List<OrderResponse>>builder()
                .result(listOrder.stream()
                        .map(orderMapper::toOrderResponse).toList())
                .build();
    }

    public ApiResponse<OrderResponse> saveOrder(OrderRequest orderRequest) {
        var order = orderMapper.toOrder(orderRequest);

        return ApiResponse.<OrderResponse>builder()
                .result(orderMapper.toOrderResponse(orderRepository.save(order)))
                .build();
    }
}
