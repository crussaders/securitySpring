package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Payment;
import com.manish.spring.security.Repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public Payment createPayment(Payment payment) {
        payment.setPaymentStatus("SUCCESS");
        return paymentRepository.save(payment);
    }

    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }
}
