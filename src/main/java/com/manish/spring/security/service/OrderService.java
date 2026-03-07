package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Order;
import com.manish.spring.security.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }

    public Order createOrder(Order order) {
        order.setStatus("CREATED");
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long id, String status) {

        Order order = getOrder(id);
        order.setStatus(status);

        return orderRepository.save(order);
    }
}
