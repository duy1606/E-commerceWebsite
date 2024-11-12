package com.example.identityservice.enums;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public enum TypeOfUser {
    CUSTOMER("Khách hàng"),
    STAFF("Nhân viên"),
    MANAGER("Quản lý"),
    ADMIN("Quản trị");
    String name;

}
