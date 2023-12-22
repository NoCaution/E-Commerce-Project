package com.example.orderservice.Service;

import com.example.commonservice.Entity.Enum.OrderStatus;
import com.example.orderservice.Entity.Order;
import com.example.orderservice.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;


    public Order createOrUpdateOrder(Order order) {
        return orderRepository.save(order);
    }

    public Order getOrderById(UUID id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order getLoggedInUserOrderById(UUID orderId, UUID userId) {
        return orderRepository.findOrderByIdAndUserId(orderId, userId);
    }

    public List<Order> getOrdersByUserId(UUID userId) {
        return orderRepository.findOrdersByUserId(userId);
    }

    public List<Order> getOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getNewOrders() {
        return orderRepository.findOrdersByStatus(OrderStatus.NEW_ORDER);
    }
}
