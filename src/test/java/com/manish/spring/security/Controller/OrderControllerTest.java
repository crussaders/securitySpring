package com.manish.spring.security.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manish.spring.security.Entity.Order;
import com.manish.spring.security.exception.ResourceNotFoundException;
import com.manish.spring.security.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    private Order buildOrder(Long id, String status) {
        return new Order(id, null, new BigDecimal("100.00"), status, null, null, null);
    }

    @Test
    void getOrders_returnsListOfOrders() throws Exception {
        Order order = buildOrder(1L, "CREATED");
        when(orderService.getOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    void getOrder_returnsOrder_whenFound() throws Exception {
        Order order = buildOrder(1L, "CREATED");
        when(orderService.getOrder(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }

    @Test
    void getOrder_returns404_whenNotFound() throws Exception {
        when(orderService.getOrder(99L)).thenThrow(new ResourceNotFoundException("Order", 99L));

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_returnsCreatedOrder() throws Exception {
        Order input = buildOrder(null, null);
        Order saved = buildOrder(1L, "CREATED");
        when(orderService.createOrder(any(Order.class))).thenReturn(saved);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void updateStatus_returnsUpdatedOrder() throws Exception {
        Order updated = buildOrder(1L, "SHIPPED");
        when(orderService.updateOrderStatus(eq(1L), eq("SHIPPED"))).thenReturn(updated);

        mockMvc.perform(put("/orders/1/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void updateStatus_returns404_whenOrderNotFound() throws Exception {
        when(orderService.updateOrderStatus(eq(99L), any(String.class)))
                .thenThrow(new ResourceNotFoundException("Order", 99L));

        mockMvc.perform(put("/orders/99/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_returns200_onSuccess() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteOrder_returns409_whenOrderHasItems() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete order with associated order items"))
                .when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteOrder_returns409_whenOrderHasPayment() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete order with associated payments"))
                .when(orderService).deleteOrder(2L);

        mockMvc.perform(delete("/orders/2"))
                .andExpect(status().isConflict());
    }
}
