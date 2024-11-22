package com.example.orderservice.exception;

import com.example.orderservice.enums.ErrorCode;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(makeFinal = true)
public class AppException extends RuntimeException {
    ErrorCode errorCode;
}
