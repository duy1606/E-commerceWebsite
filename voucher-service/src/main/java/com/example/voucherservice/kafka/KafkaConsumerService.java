package com.example.voucherservice.kafka;

import com.example.dtocommon.kafka.VoucherCheckEvent;
import com.example.voucherservice.entity.Voucher;
import com.example.voucherservice.repository.VoucherRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private VoucherRepository voucherRepository;

    @KafkaListener(topics = "order-create",groupId = "voucher-group")
    public void hanleEventFromOrder(@Payload String message) throws JsonProcessingException {
        VoucherCheckEvent voucherCheckEvent = objectMapper.readValue(message, VoucherCheckEvent.class);
        Voucher voucher =voucherRepository.findById(voucherCheckEvent.getVoucherID()).get();
    }
}
