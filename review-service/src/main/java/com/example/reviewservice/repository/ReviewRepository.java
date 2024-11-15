package com.example.reviewservice.repository;

import com.example.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review,String> {
    List<Review> findAllByProductId(String productId);
    List<Review> findAllByAccountID(String accountID);
}
