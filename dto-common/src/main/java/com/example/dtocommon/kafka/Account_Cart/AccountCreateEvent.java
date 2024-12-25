package com.example.dtocommon.kafka.Account_Cart;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AccountCreateEvent {
    String accountID;
}
