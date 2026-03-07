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

    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Payment updatePayment(Long id, Payment payment) {
        Payment existing = getPayment(id);
        existing.setPaymentMethod(payment.getPaymentMethod());
        existing.setAmount(payment.getAmount());
        existing.setPaymentDate(payment.getPaymentDate());
        return paymentRepository.save(existing);
    }

    public void deletePayment(Long id) {
        getPayment(id);
        paymentRepository.deleteById(id);
    }
}
