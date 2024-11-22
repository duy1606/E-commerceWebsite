package com.example.orderservice.kafka;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "order-create";
    public void sendOrderRequest(Object object) throws JsonProcessingException {
        String jsonMessage = new ObjectMapper().writeValueAsString(object);  // Chuyển đối tượng thành JSON
        kafkaTemplate.send(TOPIC, jsonMessage);
    }
}
