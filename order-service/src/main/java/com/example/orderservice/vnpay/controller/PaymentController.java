package com.example.orderservice.vnpay.controller;

import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.vnpay.config.Config;
import com.example.orderservice.vnpay.dto.PaymentInfo;
import com.example.orderservice.vnpay.dto.PaymentRequest;
import com.example.orderservice.vnpay.dto.PaymentResDto;
import com.example.orderservice.vnpay.dto.PaymentResponse;
import com.example.orderservice.vnpay.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;


@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/create_payment")
    public ApiResponse<?> createPayment(HttpServletRequest req, HttpServletResponse resp, @RequestBody PaymentRequest paymentRequest) throws UnsupportedEncodingException {
        return paymentService.createPayment(req, resp, paymentRequest);
    }

    @GetMapping("/payment-info")
    public PaymentResponse paymentInfo(
        @RequestParam String vnp_Amount,
        @RequestParam String vnp_BankCode,
        @RequestParam String vnp_TxnRef,
        @RequestParam String vnp_CardType,
        @RequestParam String vnp_ResponseCode
    ){
        var a = PaymentInfo.builder()
                .vnp_Amount(vnp_Amount)
                .vnp_BankCode(vnp_BankCode)
                .vnp_TxnRef(vnp_TxnRef)
                .vnp_CardType(vnp_CardType)
                .vnp_ResponseCode(vnp_ResponseCode)
                .build();
        return paymentService.handlePaymentInfo(a);
    }
}
