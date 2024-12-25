package com.example.identityservice.controller;

import com.example.dtocommon.kafka.User_Account.CustomerCreateEvent;
import com.example.identityservice.dto.request.ChangePasswordRequest;
import com.example.identityservice.dto.request.UserUpdateRequest;
import com.example.identityservice.dto.response.ApiResponse;
import com.example.identityservice.dto.response.UserResponse;
import com.example.identityservice.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class UserController {
    UserService userService;
    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .build();
    }
    @PostMapping("/create")
   public ApiResponse<?> createUser(@RequestBody CustomerCreateEvent event) throws JsonProcessingException {
        return userService.createUserWithKafka(event);
    }
    @DeleteMapping("/{userID}")
    public ApiResponse<UserResponse> deleteUser(@PathVariable String userID){
        userService.deleteUser(userID);
        return ApiResponse.<UserResponse>builder()
                .message("User with ID " + userID + " deleted")
                .build();
    }
    @PutMapping("/{userID}")
    public ApiResponse<UserResponse> updateUser(@PathVariable String userID, @RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userID, request))
                .build();
    }
    @GetMapping("fbid/{userID}")
    public ApiResponse<UserResponse> getUserByID(@PathVariable String userID){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserByID(userID))
                .build();
    }
}

