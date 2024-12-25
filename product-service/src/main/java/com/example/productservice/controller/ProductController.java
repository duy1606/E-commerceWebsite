package com.example.productservice.controller;

import com.example.productservice.dto.request.*;
import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.dto.response.ProductResponse;


import com.example.productservice.service.ProductService;

import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/product")
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class ProductController {
    ProductService productService;
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
    @GetMapping("/fbn/{name}")
    public ApiResponse<ProductResponse> fbmProduct(@PathVariable String name) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.findByName(name))
                .build();
    }
    @GetMapping("/search/{keyword}")
    public ApiResponse<List<ProductResponse>> searchProduct(@PathVariable String keyword) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.findByNameContainingIgnoreCase(keyword))
                .build();
    }
    @GetMapping("/page")
    public  ApiResponse<Page<ProductResponse>> getProductPage(@RequestParam int page, @RequestParam int size) {
        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.getProductsByPage(page, size))
                .build();
    }
}
