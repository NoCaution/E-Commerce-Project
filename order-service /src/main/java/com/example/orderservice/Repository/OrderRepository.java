package com.example.orderservice.Repository;

import com.example.commonservice.Entity.Enum.OrderStatus;
import com.example.orderservice.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findOrdersByUserId(UUID userId);

    List<Order> findOrdersByStatus(OrderStatus orderStatus);

    Order findOrderByIdAndUserId(UUID orderId, UUID userId);
}
