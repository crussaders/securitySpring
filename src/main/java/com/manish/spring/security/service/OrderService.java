package com.manish.spring.security.service;

import com.manish.spring.security.Entity.Order;
import com.manish.spring.security.Repository.OrderRepository;
import com.manish.spring.security.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
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
