package com.example.orderservice.service;

import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.OrderResponse;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.kafka.KafkaProducerService;
import com.example.dtocommon.kafka.InventoryCheckEvent;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {
    OrderRepository orderRepository;
    OrderMapper orderMapper;
    KafkaProducerService kafkaProducerService;
    public ApiResponse<List<OrderResponse>> findByDateOrder(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        var listOrder = orderRepository.findByDateCreatedBetween(startOfDay, endOfDay);

        return ApiResponse.<List<OrderResponse>>builder()
                .result(listOrder.stream()
                        .map(orderMapper::toOrderResponse).toList())
                .build();
    }
                                //FeignClient
//    public ApiResponse<OrderResponse> createOrder(OrderRequest request) {
//        var order = orderMapper.toOrder(request);
//        var orderDetails = order.getOrderDetails();
//        orderDetails.forEach(orderDetail -> {
//            ApiResponse<Boolean> check = productServiceClient.sellProduct(SellProductRequest.builder()
//                    .id(orderDetail.getProductID())
//                    .color(orderDetail.getColor())
//                    .size(Size.valueOf(orderDetail.getSize()))
//                    .quantity(orderDetail.getQuantity())
//                    .build());
//            if(!check.getResult()) {
//                throw  new AppException(ErrorCode.ERROR);
//            }
//        });
//        order.setStatus(OrderStatus.ORDERED);
//        orderRepository.save(order);
//        return ApiResponse.<OrderResponse>builder()
//                .result(orderMapper.toOrderResponse(order))
//                .build();
//    }
                                //Kafka
    public ApiResponse<OrderResponse> createOrder(OrderRequest orderRequest) {
        var order = orderMapper.toOrder(orderRequest);
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        var orderDetails = order.getOrderDetails();
        orderDetails.forEach(orderDetail -> {
            try {
                kafkaProducerService.sendOrderRequest(InventoryCheckEvent.builder()
                                .id(order.getId())
                                .color(orderDetail.getColor())
                                .size(orderDetail.getSize())
                                .quantity(orderDetail.getQuantity())
                                .productID(orderDetail.getProductID())
                        .build());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return ApiResponse.<OrderResponse>builder()
                .result(orderMapper.toOrderResponse(order))
                .build();
    }
}
