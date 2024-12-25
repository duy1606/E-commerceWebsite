package com.example.orderservice.kafka.consumer;

import com.example.dtocommon.kafka.Order_Cart.OrderSuccessfully;
import com.example.dtocommon.kafka.Order_Product.InventoryCheckEvent;
import com.example.dtocommon.kafka.Order_Product.SellProductEvent;
import com.example.dtocommon.kafka.Order_Product.SellProductResultEvent;
import com.example.dtocommon.kafka.Order_Voucher.UseVoucherEvent;
import com.example.dtocommon.kafka.Order_Voucher.UseVoucherResultEvent;
import com.example.orderservice.entity.OrderDetail;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.enums.PaymentStatus;
import com.example.orderservice.kafka.config.JsonConverter;
import com.example.orderservice.kafka.producer.OrderProducer;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.vnpay.controller.PaymentController;
import com.example.orderservice.vnpay.dto.PaymentRequest;
import com.example.orderservice.vnpay.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderConsumer {
    final OrderRepository orderRepository;
    JsonConverter jsonConverter;
    OrderProducer orderProducer;

    SimpMessagingTemplate messagingTemplate;
    PaymentService paymentService;

    //    lắng nghe use-voucher
    @KafkaListener(topics = "use-voucher-response", groupId = "order-group")
    public void handleRollbackOrder(String data) throws UnsupportedEncodingException {
        UseVoucherResultEvent event = jsonConverter.fromJson(data, UseVoucherResultEvent.class);

        var order = orderRepository.findById(event.getOrderID())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (event.isResult()) {
            //            send notification to font-end
            if (order.getPayment().toString().equals("VN_PAY")) {
                orderRepository.save(order);
                messagingTemplate.convertAndSend("/topic/orders", "Order successfully processed," + order.getId() + "," + order.getTotalPrice());
            } else {
                order.setStatus(OrderStatus.ORDERED);
                orderRepository.save(order);
                messagingTemplate.convertAndSend("/topic/orders", "Order successfully processed");
            }
//            send event order successfully to cart
            //            send event order successfully to cart
            order.getOrderDetails().forEach(orderDetail -> {
                OrderSuccessfully orderSuccessfully = OrderSuccessfully.builder()
                        .accountID(order.getAccountID())
                        .productID(orderDetail.getProductID())
                        .color(orderDetail.getColor())
                        .size(orderDetail.getSize())
                        .build();
                orderProducer.sendOrderSuccessfullyToCart(orderSuccessfully);
            });

        } else {
//            rollback product
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

            orderRepository.delete(order);
            messagingTemplate.convertAndSend("/topic/orders", "Fail(Voucher sold out)");
        }
    }

    @KafkaListener(topics = "sell-product-result", groupId = "order-group")
    public void handleSellProductResult(String data) {
        SellProductResultEvent event = jsonConverter.fromJson(data, SellProductResultEvent.class);
        var order = orderRepository.findById(event.getOrderID())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (event.isResult()) {
//            neu khong dung voucher
            if (order.getVoucherID().equals("") || order.getVoucherID() == null) {

                //            send notification to font-end
                if (order.getPayment().toString().equals("VN_PAY")) {
                    orderRepository.save(order);
                    messagingTemplate.convertAndSend("/topic/orders", "Order successfully processed," + order.getId() + "," + order.getTotalPrice());
                } else {
                    order.setStatus(OrderStatus.ORDERED);
                    orderRepository.save(order);
                    messagingTemplate.convertAndSend("/topic/orders", "Order successfully processed");
                }

//            send event order successfully to cart
                //            send event order successfully to cart
                order.getOrderDetails().forEach(orderDetail -> {
                    OrderSuccessfully orderSuccessfully = OrderSuccessfully.builder()
                            .accountID(order.getAccountID())
                            .productID(orderDetail.getProductID())
                            .color(orderDetail.getColor())
                            .size(orderDetail.getSize())
                            .build();
                    orderProducer.sendOrderSuccessfullyToCart(orderSuccessfully);
                });

                return;
            }

            UseVoucherEvent useVoucherEvent = UseVoucherEvent.builder()
                    .orderID(order.getId())
                    .voucherID(order.getVoucherID())
                    .build();
            orderProducer.sendOrderSuccessToVoucher(useVoucherEvent);
        } else {
            orderRepository.delete(order);
            messagingTemplate.convertAndSend("/topic/orders", "Fail(Product sold out)");
        }
    }
}
