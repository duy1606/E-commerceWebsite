package com.example.productservice.controller;

import com.example.productservice.dto.request.*;
import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.dto.response.ProductResponse;


import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.service.ProductService;

import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/product")
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    ProductService productService;
    ProductMapper productMapper;
    KafkaTemplate<String, Boolean> kafkaTemplate;
    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }
    @GetMapping
    public ApiResponse<List<ProductResponse>> getProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getAllProducts())
                .build();
    }
    @GetMapping("/{productID}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable String productID) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(productID))
                .build();
    }
    @PutMapping("/{productID}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable String productID, @RequestBody ProductUpdateRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productID, request))
                .build();
    }
    @DeleteMapping("/{productID}")
    public ApiResponse<?> deleteProduct(@PathVariable String productID) {
        productService.deleteProduct(productID);
        return ApiResponse.builder()
                .message("Product with ID " + productID + " was deleted")
                .build();
    }
    @PutMapping("/procure")
    public ApiResponse<ProductResponse> procureProduct(@RequestBody ProcureProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.procureProduct(request))
                .build();
    }
    @PutMapping("/sell")
    public ApiResponse<?> sellProduct(@RequestBody SellProductRequest request) {
        return ApiResponse.<Boolean>builder()
                .result(productService.sellProduct(request))
                .build();
    }


}
