package com.example.identityservice.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_FOUND(1001,"Not found",HttpStatus.NOT_FOUND);
    int code;
    String message;
    HttpStatus httpStatus;
}
