package com.manish.spring.security.Controller;

import com.manish.spring.security.Entity.Payment;
import com.manish.spring.security.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Payment makePayment(@RequestBody Payment payment) {
        return paymentService.createPayment(payment);
    }

    @GetMapping
    public List<Payment> getPayments() {
        return paymentService.getPayments();
    }
}
