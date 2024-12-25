package com.example.dtocommon.kafka.User_Account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerCreateEvent {
    String id;
    String name;
    int age;
    String address;
    String typeOfUser;
    String phoneNumber;
    String email;
    boolean gender;
    String password;
}
