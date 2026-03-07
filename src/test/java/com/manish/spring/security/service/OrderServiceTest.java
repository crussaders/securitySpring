package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Order;
import com.manish.spring.security.Entity.OrderItem;
import com.manish.spring.security.Entity.Payment;
import com.manish.spring.security.Repository.OrderRepository;
import com.manish.spring.security.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    // ─── getOrders ───────────────────────────────────────────────────────────

    @Test
    void getOrders_returnsAllOrders() {
        // Arrange
        Order order1 = new Order(1L, null, BigDecimal.TEN, "CREATED", LocalDateTime.now(), null, null);
        Order order2 = new Order(2L, null, BigDecimal.ONE, "SHIPPED", LocalDateTime.now(), null, null);
        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        // Act
        List<Order> result = orderService.getOrders();

        // Assert
        assertThat(result).hasSize(2).containsExactly(order1, order2);
        verify(orderRepository).findAll();
    }

    @Test
    void getOrders_returnsEmptyListWhenNoOrders() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.getOrders();

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository).findAll();
    }

    // ─── getOrder ────────────────────────────────────────────────────────────

    @Test
    void getOrder_returnsOrderWhenFound() {
        // Arrange
        Order order = new Order(1L, null, BigDecimal.TEN, "CREATED", LocalDateTime.now(), null, null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        Order result = orderService.getOrder(1L);

        // Assert
        assertThat(result).isEqualTo(order);
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrder_throwsResourceNotFoundExceptionWhenMissing() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrder(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order")
                .hasMessageContaining("99");
        verify(orderRepository).findById(99L);
    }

    // ─── createOrder ─────────────────────────────────────────────────────────

    @Test
    void createOrder_setsStatusToCreatedAndSaves() {
        // Arrange
        Order input = new Order(null, null, BigDecimal.TEN, null, LocalDateTime.now(), null, null);
        Order saved = new Order(1L, null, BigDecimal.TEN, "CREATED", LocalDateTime.now(), null, null);
        when(orderRepository.save(input)).thenReturn(saved);

        // Act
        Order result = orderService.createOrder(input);

        // Assert
        assertThat(input.getStatus()).isEqualTo("CREATED");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("CREATED");
        verify(orderRepository).save(input);
    }

    // ─── updateOrderStatus ───────────────────────────────────────────────────

    @Test
    void updateOrderStatus_updatesStatusAndReturnsOrder() {
        // Arrange
        Order existing = new Order(1L, null, BigDecimal.TEN, "CREATED", LocalDateTime.now(), null, null);
        Order updated = new Order(1L, null, BigDecimal.TEN, "SHIPPED", LocalDateTime.now(), null, null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(existing)).thenReturn(updated);

        // Act
        Order result = orderService.updateOrderStatus(1L, "SHIPPED");

        // Assert
        assertThat(existing.getStatus()).isEqualTo("SHIPPED");
        assertThat(result.getStatus()).isEqualTo("SHIPPED");
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(existing);
    }

    @Test
    void updateOrderStatus_throwsResourceNotFoundExceptionWhenMissing() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderStatus(99L, "SHIPPED"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order");
        verify(orderRepository, never()).save(any());
    }

    // ─── deleteOrder ─────────────────────────────────────────────────────────

    @Test
    void deleteOrder_deletesSuccessfullyWhenNoItemsAndNoPayment() {
        // Arrange
        Order order = new Order(1L, null, BigDecimal.TEN, "CREATED", LocalDateTime.now(),
                Collections.emptyList(), null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.deleteOrder(1L);

        // Assert
        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_throwsResourceNotFoundExceptionWhenMissing() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.deleteOrder(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order");
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void deleteOrder_throwsConflictWhenOrderHasItems() {
        // Arrange
        OrderItem item = new OrderItem(1L, null, null, 2, BigDecimal.TEN);
        Order order = new Order(1L, null, BigDecimal.TEN, "CREATED", LocalDateTime.now(),
                List.of(item), null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.deleteOrder(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
        verify(orderRepository, never()).delete(any());
    }

    @Test
    void deleteOrder_throwsConflictWhenOrderHasPayment() {
        // Arrange
        Payment payment = new Payment(1L, null, "CARD", "SUCCESS", BigDecimal.TEN, LocalDateTime.now());
        Order order = new Order(1L, null, BigDecimal.TEN, "CREATED", LocalDateTime.now(),
                Collections.emptyList(), payment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.deleteOrder(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
        verify(orderRepository, never()).delete(any());
    }
}
