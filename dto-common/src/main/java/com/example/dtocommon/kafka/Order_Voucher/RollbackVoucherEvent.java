package com.example.dtocommon.kafka.Order_Voucher;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RollbackVoucherEvent {
    String voucherID;
}
