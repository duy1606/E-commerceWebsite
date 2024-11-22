package com.example.productservice.kafka;

import com.example.dtocommon.kafka.OrderCheckEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String TOPIC = "product-available";
    public void sendInventoryUpdateEvent (OrderCheckEvent event) throws JsonProcessingException {
        String message =objectMapper.writeValueAsString(event);
        kafkaTemplate.send(TOPIC,message);
    }
}
