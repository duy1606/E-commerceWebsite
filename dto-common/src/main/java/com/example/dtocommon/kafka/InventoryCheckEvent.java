package com.example.dtocommon.kafka;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryCheckEvent {
    String id;
    String productID;
    int quantity;
    String color;
    String size;
}
