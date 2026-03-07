package com.manish.spring.security.Repository;

import com.manish.spring.security.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Optional useful methods
    List<Payment> findByPaymentStatus(String paymentStatus);

    List<Payment> findByOrderId(Long orderId);

}