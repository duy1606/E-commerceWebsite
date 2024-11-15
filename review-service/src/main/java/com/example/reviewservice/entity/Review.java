package com.example.reviewservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class Review {
    @Id
    String id;
    String productID;
    String accountID;
    String content;
    double rating;
    LocalDate createdAt;
}
