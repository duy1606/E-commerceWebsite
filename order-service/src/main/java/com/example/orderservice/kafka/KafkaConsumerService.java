package com.example.orderservice.kafka;


import com.example.dtocommon.kafka.OrderCheckEvent;
import com.example.orderservice.entity.Order;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "product-available", groupId = "order-group")
    public void handleEventFromProduct(@Payload String message) throws JsonProcessingException {
        OrderCheckEvent inventoryUpdateEvent = objectMapper.readValue(message, OrderCheckEvent.class);
        if (inventoryUpdateEvent.isInvalid()) {
            Order order = orderRepository.findById(inventoryUpdateEvent.getOrderID()).orElse(null);
            order.setStatus(OrderStatus.ORDERED);
            orderRepository.save(order);
            return;
        }
        orderRepository.deleteById(inventoryUpdateEvent.getOrderID());
    }
}
