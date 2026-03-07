package com.manish.spring.security.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manish.spring.security.Entity.Payment;
import com.manish.spring.security.exception.ResourceNotFoundException;
import com.manish.spring.security.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();
    }

    private Payment buildPayment(Long id, String status) {
        // order is set to null to avoid circular serialization with Order<->Payment
        return new Payment(id, null, "CREDIT_CARD", status, new BigDecimal("250.00"), null);
    }

    @Test
    void makePayment_returnsPaymentWithSuccessStatus() throws Exception {
        Payment input = buildPayment(null, null);
        Payment saved = buildPayment(1L, "SUCCESS");
        when(paymentService.createPayment(any(Payment.class))).thenReturn(saved);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));
    }

    @Test
    void getPayments_returnsListOfPayments() throws Exception {
        Payment payment = buildPayment(1L, "SUCCESS");
        when(paymentService.getPayments()).thenReturn(List.of(payment));

        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].paymentStatus").value("SUCCESS"));
    }

    @Test
    void getPayment_returnsPayment_whenFound() throws Exception {
        Payment payment = buildPayment(1L, "SUCCESS");
        when(paymentService.getPayment(1L)).thenReturn(payment);

        mockMvc.perform(get("/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.amount").value(250.00));
    }

    @Test
    void getPayment_returns404_whenNotFound() throws Exception {
        when(paymentService.getPayment(99L)).thenThrow(new ResourceNotFoundException("Payment", 99L));

        mockMvc.perform(get("/payments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePayment_returnsUpdatedPayment() throws Exception {
        Payment input = buildPayment(null, "REFUNDED");
        Payment updated = buildPayment(1L, "REFUNDED");
        when(paymentService.updatePayment(eq(1L), any(Payment.class))).thenReturn(updated);

        mockMvc.perform(put("/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentStatus").value("REFUNDED"));
    }

    @Test
    void updatePayment_returns404_whenNotFound() throws Exception {
        Payment input = buildPayment(null, "REFUNDED");
        when(paymentService.updatePayment(eq(99L), any(Payment.class)))
                .thenThrow(new ResourceNotFoundException("Payment", 99L));

        mockMvc.perform(put("/payments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePayment_returns200_onSuccess() throws Exception {
        doNothing().when(paymentService).deletePayment(1L);

        mockMvc.perform(delete("/payments/1"))
                .andExpect(status().isOk());
    }
}
