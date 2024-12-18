package com.example.productservice.controller;

import com.example.productservice.dto.request.ProcureProductRequest;
import com.example.productservice.dto.request.ProductCreateRequest;
import com.example.productservice.dto.request.ProductUpdateRequest;
import com.example.productservice.dto.request.SellProductRequest;
import com.example.productservice.dto.response.ApiResponse;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.service.ProductService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    @PutMapping("/sell")
    public ApiResponse<?> sellProduct(@RequestBody SellProductRequest request) {
        return productService.sellProduct(request);
    }
}
