package com.example.orderservice.repository.httpClient;
import com.example.orderservice.configuration.AuthenticationRequestInterceptor;
import com.example.orderservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service",url = "http://localhost:8081/product-service",configuration = AuthenticationRequestInterceptor.class)
public interface ProductServiceClient {
    @PutMapping(value = "/product/sell",produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> sellProduct(@RequestBody SellProductRequest sellProductRequest);
}
