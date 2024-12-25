package com.example.notificationservice.kafka;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.enums.NotificationType;
import com.example.notificationservice.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {
    NotificationRepository notificationRepository;
    ObjectMapper objectMapper;
    @KafkaListener(topics = "send-notification",groupId = "notification-group")
    public void sendNotification(@Payload String message) throws JsonProcessingException {
        OrderResponseEvent orderResponseEvent = objectMapper.readValue(message, OrderResponseEvent.class);
        Notification notification = new Notification();
        notification.setAccountID(orderResponseEvent.getCustomerID());
        log.info(orderResponseEvent.getCustomerID());
        notification.setType(NotificationType.INFO);
        notification.setRead(false);
        notification.setTimeStamp(LocalDateTime.now());
        notification.setContent("Your order has been created successfully with id " + orderResponseEvent.getOrderID());

        notificationRepository.save(notification);
    }
}
