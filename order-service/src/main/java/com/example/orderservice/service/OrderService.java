package com.example.orderservice.service;

import com.example.dtocommon.kafka.Order_Product.InventoryCheckEvent;
import com.example.dtocommon.kafka.Order_Product.SellProductEvent;
import com.example.dtocommon.kafka.Order_Voucher.RollbackVoucherEvent;
import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.request.OrderRollbackRequest;
import com.example.orderservice.dto.request.UpdateStatusRequest;
import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.exception.AppException;
import com.example.orderservice.kafka.producer.OrderProducer;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    OrderRepository orderRepository;
    OrderMapper orderMapper;
    OrderProducer orderProducer;

    public ApiResponse<List<OrderResponse>> findByDateOrder(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        var listOrder = orderRepository.findByDateCreatedBetween(startOfDay, endOfDay);

        return ApiResponse.<List<OrderResponse>>builder()
                .result(listOrder.stream()
                        .map(orderMapper::toOrderResponse).toList())
                .build();
    }

    public ApiResponse<?> updateOrderStatus(UpdateStatusRequest request) {
        OrderStatus orderStatus = OrderStatus.valueOf(request.getStatus());

        Optional<Order> optionalOrder = orderRepository.findById(request.getOrderID());
        if (optionalOrder.isEmpty()) {
            return ApiResponse.builder()
                    .code(404) // Mã lỗi phù hợp, ví dụ 404 Not Found
                    .result("Order not found")
                    .build();
        }
        Order order = optionalOrder.get();

        order.setStatus(orderStatus);
        orderRepository.save(order);
        return ApiResponse.builder()
                .message("Order-status updated")
                .build();
    }

    public ApiResponse<OrderResponse> saveOrder(OrderRequest orderRequest) throws JsonProcessingException {
        var order = orderMapper.toOrder(orderRequest);
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        // Gửi sự kiện "order-created" lên Kafka
        List<InventoryCheckEvent> events = new ArrayList<>();
        order.getOrderDetails().forEach(orderDetail ->
                events.add(
                        InventoryCheckEvent.builder()
                                .productID(orderDetail.getProductID())
                                .color(orderDetail.getColor())
                                .size(orderDetail.getSize())
                                .quantity(orderDetail.getQuantity())
                                .build()
                )
        );

        SellProductEvent event1 = new SellProductEvent(order.getId(), events);
        orderProducer.sendCreateOrderToProduct(event1);

        return ApiResponse.<OrderResponse>builder()
                .result(orderMapper.toOrderResponse(order))
                .build();
    }

    public ApiResponse<?> rollbackOrder(OrderRollbackRequest request) {
        Optional<Order> optionalOrder = orderRepository.findById(request.getOrderID());
        if (optionalOrder.isEmpty()) {
            return ApiResponse.builder()
                    .code(404) // Mã lỗi phù hợp, ví dụ 404 Not Found
                    .result("Order not found")
                    .build();
        }

        Order order = optionalOrder.get();

//        rollback product
        List<InventoryCheckEvent> inventoryCheckEventList = new ArrayList<>();
        order.getOrderDetails().stream().forEach(
                orderDetail -> inventoryCheckEventList.add(
                        InventoryCheckEvent.builder()
                                .productID(orderDetail.getProductID())
                                .color(orderDetail.getColor())
                                .quantity(orderDetail.getQuantity())
                                .size(orderDetail.getSize())
                                .build()
                )
        );

        SellProductEvent sellProductEvent = SellProductEvent.builder()
                .orderID(order.getId())
                .list(inventoryCheckEventList)
                .build();
        orderProducer.sendRollBackProduct(sellProductEvent);

//    rollback voucher
        if(order.getVoucherID().equals("") || order.getVoucherID() == null) {

        }else {
            orderProducer.sendRollbackVoucher(RollbackVoucherEvent.builder()
                    .voucherID(order.getVoucherID())
                    .build());
        }

        orderRepository.delete(order);

        return ApiResponse.builder()
                .result("Rollback order successfully")
                .build();
    }

}
