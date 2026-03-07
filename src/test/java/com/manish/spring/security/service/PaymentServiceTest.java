package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Payment;
import com.manish.spring.security.Repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    // ─── createPayment ───────────────────────────────────────────────────────

    @Test
    void createPayment_setsStatusToSuccessAndSaves() {
        // Arrange
        Payment input = new Payment(null, null, "CARD", null, BigDecimal.TEN, LocalDateTime.now());
        Payment saved = new Payment(1L, null, "CARD", "SUCCESS", BigDecimal.TEN, LocalDateTime.now());
        when(paymentRepository.save(input)).thenReturn(saved);

        // Act
        Payment result = paymentService.createPayment(input);

        // Assert
        assertThat(input.getPaymentStatus()).isEqualTo("SUCCESS");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPaymentStatus()).isEqualTo("SUCCESS");
        verify(paymentRepository).save(input);
    }

    // ─── getPayments ─────────────────────────────────────────────────────────

    @Test
    void getPayments_returnsAllPayments() {
        // Arrange
        Payment p1 = new Payment(1L, null, "CARD", "SUCCESS", BigDecimal.TEN, LocalDateTime.now());
        Payment p2 = new Payment(2L, null, "CASH", "SUCCESS", BigDecimal.ONE, LocalDateTime.now());
        when(paymentRepository.findAll()).thenReturn(List.of(p1, p2));

        // Act
        List<Payment> result = paymentService.getPayments();

        // Assert
        assertThat(result).hasSize(2).containsExactly(p1, p2);
        verify(paymentRepository).findAll();
    }

    @Test
    void getPayments_returnsEmptyListWhenNoPayments() {
        // Arrange
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Payment> result = paymentService.getPayments();

        // Assert
        assertThat(result).isEmpty();
        verify(paymentRepository).findAll();
    }

    // ─── getPayment ──────────────────────────────────────────────────────────

    @Test
    void getPayment_returnsPaymentWhenFound() {
        // Arrange
        Payment payment = new Payment(1L, null, "CARD", "SUCCESS", BigDecimal.TEN, LocalDateTime.now());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        Payment result = paymentService.getPayment(1L);

        // Assert
        assertThat(result).isEqualTo(payment);
        verify(paymentRepository).findById(1L);
    }

    @Test
    void getPayment_throwsRuntimeExceptionWhenMissing() {
        // Arrange
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPayment(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
        verify(paymentRepository).findById(99L);
    }

    // ─── updatePayment ───────────────────────────────────────────────────────

    @Test
    void updatePayment_updatesAllFieldsAndReturnsPayment() {
        // Arrange
        LocalDateTime originalDate = LocalDateTime.now().minusDays(1);
        LocalDateTime newDate = LocalDateTime.now();
        Payment existing = new Payment(1L, null, "CASH", "SUCCESS", BigDecimal.ONE, originalDate);
        Payment updateRequest = new Payment(null, null, "CARD", "REFUNDED", BigDecimal.TEN, newDate);
        Payment saved = new Payment(1L, null, "CARD", "REFUNDED", BigDecimal.TEN, newDate);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentRepository.save(existing)).thenReturn(saved);

        // Act
        Payment result = paymentService.updatePayment(1L, updateRequest);

        // Assert
        assertThat(existing.getPaymentMethod()).isEqualTo("CARD");
        assertThat(existing.getPaymentStatus()).isEqualTo("REFUNDED");
        assertThat(existing.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(existing.getPaymentDate()).isEqualTo(newDate);
        assertThat(result.getPaymentMethod()).isEqualTo("CARD");
        assertThat(result.getPaymentStatus()).isEqualTo("REFUNDED");
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(existing);
    }

    @Test
    void updatePayment_throwsRuntimeExceptionWhenMissing() {
        // Arrange
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.updatePayment(99L, new Payment()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
        verify(paymentRepository, never()).save(any());
    }

    // ─── deletePayment ───────────────────────────────────────────────────────

    @Test
    void deletePayment_deletesSuccessfullyWhenFound() {
        // Arrange
        Payment payment = new Payment(1L, null, "CARD", "SUCCESS", BigDecimal.TEN, LocalDateTime.now());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        doNothing().when(paymentRepository).deleteById(1L);

        // Act
        paymentService.deletePayment(1L);

        // Assert
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).deleteById(1L);
    }

    @Test
    void deletePayment_throwsRuntimeExceptionWhenMissing() {
        // Arrange
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.deletePayment(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
        verify(paymentRepository, never()).deleteById(any());
    }
}
