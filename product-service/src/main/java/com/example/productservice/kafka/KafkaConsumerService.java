package com.example.productservice.kafka;

import com.example.dtocommon.kafka.OrderCheckEvent;
import com.example.dtocommon.kafka.InventoryCheckEvent;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.Variant;
import com.example.productservice.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Service
public class KafkaConsumerService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private KafkaProducerService kafkaProducerService;
    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "order-create", groupId = "product-group")
    public void listenForProductResponse(@Payload String message) throws JsonProcessingException {
        InventoryCheckEvent orderCreatedEvent = objectMapper.readValue(message, InventoryCheckEvent.class);
        Product product1 = productRepository.findById(orderCreatedEvent.getProductID()).orElse(null);
        var variants = product1.getVariants();
        Variant variant = variants.stream().filter(v -> v.getSize().getName().equals(orderCreatedEvent.getSize()) && v.getColor().equals(orderCreatedEvent.getColor())).findFirst().get();
        if (variant.getInventoryQuantity() >= orderCreatedEvent.getQuantity()) {
            variant.setInventoryQuantity(variant.getInventoryQuantity() - orderCreatedEvent.getQuantity());
            productRepository.save(product1);
            kafkaProducerService.sendInventoryUpdateEvent(OrderCheckEvent.builder()
                    .invalid(true)
                    .orderID(orderCreatedEvent.getId())
                    .build());
        } else {
            kafkaProducerService.sendInventoryUpdateEvent(OrderCheckEvent.builder()
                    .invalid(false)
                    .orderID(orderCreatedEvent.getId())
                    .build());
        }

    }

}

