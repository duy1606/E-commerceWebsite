package com.example.identityservice.service;

import com.example.dtocommon.kafka.User_Account.CustomerCreateEvent;
import com.example.identityservice.dto.request.UserCreateRequest;
import com.example.identityservice.dto.request.UserUpdateRequest;
import com.example.identityservice.dto.response.ApiResponse;
import com.example.identityservice.dto.response.UserResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.enums.ErrorCode;
import com.example.identityservice.enums.TypeOfUser;
import com.example.identityservice.exception.AppException;
import com.example.identityservice.mapper.UserMapper;
import com.example.identityservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    ObjectMapper objectMapper;
    KafkaTemplate<String, String> kafkaTemplate;
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public UserResponse createUser(UserCreateRequest request) throws JsonProcessingException {
        User user = userMapper.toUser(request);
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }
    public void deleteUser(String userID) {
        userRepository.deleteById(userID);
    }
    public UserResponse updateUser(String userID, UserUpdateRequest request) {
        User user = userRepository.findById(userID).orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND));
        userMapper.updateUser(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }
    public UserResponse getUserByID(String userID) {
        User user = userRepository.findById(userID).orElseThrow(()-> new AppException(ErrorCode.NOT_FOUND));
        return userMapper.toUserResponse(user);
    }
    public ApiResponse<?> createUserWithKafka(CustomerCreateEvent request) throws JsonProcessingException {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setGender(request.isGender());
        user.setAge(request.getAge());
        user.setTypeOfUser(TypeOfUser.valueOf(request.getTypeOfUser()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        userRepository.save(user);
        request.setId(user.getId());
        kafkaTemplate.send("user-create", objectMapper.writeValueAsString(request));
        return ApiResponse.builder().message("User Created").build();
    }
}