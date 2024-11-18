package com.example.cartservice.controller;

import com.example.cartservice.dto.ApiResponse;
import com.example.cartservice.dto.request.CartCreationRequest;
import com.example.cartservice.dto.request.AddToCartRequest;
import com.example.cartservice.dto.request.DeleteCartItemRequest;
import com.example.cartservice.dto.request.EditCartItemQuantityRequest;
import com.example.cartservice.dto.response.CartResponse;
import com.example.cartservice.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @PostMapping("/add-item/{accountID}")
    ApiResponse<CartResponse> addItemToCart(@PathVariable String accountID,
                                            @RequestBody AddToCartRequest request){
        return cartService.addItemToCart(accountID, request);
    }

    @GetMapping
    ApiResponse<List<CartResponse>> getAll(){
        return cartService.getAll();
    }

    @GetMapping("/{accountID}")
    ApiResponse<CartResponse> getByAccountID(@PathVariable String accountID){
        return cartService.getByAccountID(accountID);
    }

    @PostMapping("/create")
    ApiResponse<CartResponse> createCart(@RequestBody CartCreationRequest request){
        return cartService.createCart(request);
    }

    @PutMapping("/{accountID}")
    ApiResponse<CartResponse> editCartItemQuantity(@PathVariable String accountID,
                                                   @RequestBody EditCartItemQuantityRequest request){
        return cartService.editCartItemQuantity(accountID, request);
    }

    @DeleteMapping("/delete-item/{accountID}")
    ApiResponse<CartResponse> deleteItem(@PathVariable String accountID,
                                         @RequestBody DeleteCartItemRequest request){
        return cartService.deleteItem(accountID, request);
    }
}
