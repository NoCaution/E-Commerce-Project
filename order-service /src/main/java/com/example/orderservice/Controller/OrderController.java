package com.example.orderservice.Controller;

import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Service.CommonService;
import com.example.orderservice.Entity.Dto.CreateOrderDto;
import com.example.orderservice.Entity.Dto.HandleNewOrderDto;
import com.example.commonservice.Entity.Dto.OrderDto;
import com.example.orderservice.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonService commonService;


    @PostMapping("/createOrder")
    public APIResponse createOrder(@RequestBody CreateOrderDto createOrderDto) {
        return orderService.createOrder(createOrderDto);
    }


    @GetMapping("/getLoggedInUserOrders")
    public APIResponse getLoggedInUserOrders() {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());

        return orderService.getOrdersByUserId(loggedInUserId);
    }


    @GetMapping("/getLoggedInUserOrderById/{orderId}")
    public APIResponse getLoggedInUserOrderById(@PathVariable UUID orderId) {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());

        APIResponse getOrderResponse = orderService.getOrderById(orderId);
        OrderDto orderDto = (OrderDto) getOrderResponse.getResult();

        if(orderDto.getUserId() != loggedInUserId){
            return new APIResponse(
                    HttpStatus.UNAUTHORIZED,
                    "unauthorized request"
            );
        }

        return getOrderResponse;

    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getOrderById/{id}")
    public APIResponse getOrderById(@PathVariable UUID id) {
        return orderService.getOrderById(id);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getOrdersByUserId/{userId}")
    public APIResponse getOrdersByUserId(@PathVariable UUID userId) {
        return orderService.getOrdersByUserId(userId);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getOrders")
    public APIResponse getOrders() {
        return orderService.getOrders();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getNewOrders")
    public APIResponse getNewOrders() {
        return orderService.getNewOrders();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/handleNewOrder")
    public APIResponse handleNewOrder(@RequestBody HandleNewOrderDto handleNewOrderDto) {
        return orderService.handleNewOrder(handleNewOrderDto);
    }
}
