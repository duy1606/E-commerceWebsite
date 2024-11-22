package com.example.dtocommon.kafka;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoucherCheckEvent {
    String voucherID;
    String orderID;
}
