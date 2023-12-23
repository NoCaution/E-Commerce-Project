package com.example.orderservice.Controller;

import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Entity.Dto.ProductDto;
import com.example.commonservice.Entity.Enum.OrderStatus;
import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.AppUtil;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JwtToken;
import com.example.orderservice.Entity.Dto.CreateOrderDto;
import com.example.orderservice.Entity.Dto.HandleNewOrderDto;
import com.example.commonservice.Entity.Dto.OrderDto;
import com.example.orderservice.Entity.Order;
import com.example.orderservice.Service.OrderService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    private final String productServiceUri = "http://localhost:9000/api/product/";

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomMapper customMapper;

    @Autowired
    private AppUtil appUtil;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private CommonService commonService;


    @Procedure("this is to create an order")
    @PostMapping("/createOrder")
    public APIResponse createOrder(@RequestBody CreateOrderDto createOrderDto) {
        //set user id to loggedInUserId
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());
        createOrderDto.setUserId(loggedInUserId);

        String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
        Map<UUID, Integer> productIdMap = createOrderDto.getProductList();
        List<UUID> productIdList = productIdMap.keySet().stream().toList();

        if (createOrderDto.getUserId() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "user id can not be null"
            );
        }

        if (createOrderDto.getProductList().isEmpty()) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "product list can not be empty"
            );
        }

        //build uri
        URI uri;
        try {
            //if there is only one product
            if (productIdMap.size() == 1) {
                URIBuilder uriBuilder = new URIBuilder(productServiceUri + "getProductById/" + productIdList.stream().findFirst().orElse(null));

                uri = uriBuilder.build();

            } else {
                URIBuilder uriBuilder = new URIBuilder(productServiceUri + "getProducts");

                for (UUID productId : productIdList) {
                    uriBuilder.addParameter("id", String.valueOf(productId));
                }

                uri = uriBuilder.build();
            }

        } catch (URISyntaxException e) {
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "something went wrong.."
            );
        }


        //get product list
        Request getProductsRequest = Request.Get(uri);
        APIResponse getProductsResponse = appUtil.sendRequest(getProductsRequest, token);
        if (getProductsResponse.getHttpStatus() != HttpStatus.OK) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }


        //convert api result into List<ProductDto>
        Gson gson = new Gson();
        Object resultObject = getProductsResponse.getResult();
        Type listType = new TypeToken<List<ProductDto>>() {
        }.getType();

        String json;
        //if there is only one product
        if (productIdMap.size() == 1) {
            json = gson.toJson(List.of(resultObject));
        } else {
            json = gson.toJson(resultObject);
        }

        List<ProductDto> productDtoList = gson.fromJson(json, listType);


        //check if products exist and in stock and have enough stock
        List<UUID> notFoundProductIdList = new ArrayList<>();
        List<UUID> outOfStockProductIdList = new ArrayList<>();
        List<UUID> notEnoughStockProductIdList = new ArrayList<>();
        Long totalPrice = 0L;

        for (ProductDto productDto : productDtoList) {
            UUID productId = productDto.getId();

            if (!productIdMap.containsKey(productId)) {
                notFoundProductIdList.add(productDto.getId());
                continue;
            }

            Integer quantity = productIdMap.get(productId);
            if (quantity > productDto.getStock()) {
                notEnoughStockProductIdList.add(productId);
                continue;
            }

            if (productDto.getStock() == 0) {
                outOfStockProductIdList.add(productId);
                continue;
            }

            totalPrice += productDto.getPrice();

        }

        if (!notFoundProductIdList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product(s) found",
                    notFoundProductIdList
            );
        }

        if (!outOfStockProductIdList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.OK,
                    "product(s) out of stock",
                    outOfStockProductIdList
            );
        }

        if (!notEnoughStockProductIdList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.OK,
                    "not enough stock for product(s)",
                    notEnoughStockProductIdList
            );
        }


        //create order and update product(s) stock(s)
        Order order = customMapper.map(createOrderDto, Order.class);
        order.setTotalPrice(totalPrice);

        Order createdOrder = orderService.createOrUpdateOrder(order);

        Request updateProductsRequest = Request.Put(productServiceUri + "updateProductStockByOrderId/" + createdOrder.getId());
        APIResponse updateProductsResponse = appUtil.sendRequest(updateProductsRequest, token);
        return new APIResponse(
                HttpStatus.OK,
                updateProductsResponse.getMessage()
        );
    }

    @Procedure("this is to get authenticated user's orders")
    @GetMapping("/getLoggedInUserOrders")
    public APIResponse getLoggedInUserOrders() {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());

        List<Order> orderList = orderService.getOrdersByUserId(loggedInUserId);
        if (orderList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    orderList
            );
        }

        List<OrderDto> orderDtoList = customMapper.convertList(orderList, OrderDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                orderDtoList
        );
    }

    @Procedure("this is to get authenticated user's order with given order id")
    @GetMapping("/getLoggedInUserOrderById/{orderId}")
    public APIResponse getLoggedInUserOrderById(@PathVariable UUID orderId) {
        UUID loggedInUserId = UUID.fromString(commonService.getLoggedInUserId());
        if (orderId == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order id can not be null"
            );
        }

        Order order = orderService.getLoggedInUserOrderById(orderId, loggedInUserId);
        if (order == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no order found"
            );
        }

        OrderDto orderDto = customMapper.map(order, OrderDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                orderDto
        );
    }

    @Procedure("this is to get order with given id ")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getOrderById/{id}")
    public APIResponse getOrderById(@PathVariable UUID id) {
        if (id == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order id can not be null"
            );
        }

        Order order = orderService.getOrderById(id);
        if (order == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no order found"
            );
        }

        OrderDto orderDto = customMapper.map(order, OrderDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                orderDto
        );
    }

    @Procedure("this is to get user's orders with given user id")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getOrdersByUserId/{userId}")
    public APIResponse getOrdersByUserId(@PathVariable UUID userId) {
        if (userId == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "user id can not be null"
            );
        }

        List<Order> orderList = orderService.getOrdersByUserId(userId);
        if (orderList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    orderList
            );
        }

        List<OrderDto> orderDtoList = customMapper.convertList(orderList, OrderDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                orderDtoList
        );
    }

    @Procedure("this is to get all the orders")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getOrders")
    public APIResponse getOrders() {
        List<Order> orderList = orderService.getOrders();
        if (orderList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    orderList
            );
        }

        List<OrderDto> orderDtoList = customMapper.convertList(orderList, OrderDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                orderDtoList
        );
    }

    @Procedure("this is to get the orders which are waiting for approval")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getNewOrders")
    public APIResponse getNewOrders() {
        List<Order> newOrderList = orderService.getNewOrders();
        if (newOrderList.isEmpty()) {
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    newOrderList
            );
        }

        List<OrderDto> newOrderDtoList = customMapper.convertList(newOrderList, OrderDto.class);
        return new APIResponse(
                HttpStatus.OK,
                "success",
                newOrderDtoList
        );
    }

    @Procedure("this is to approve or deny a new order")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/handleNewOrder")
    public APIResponse handleNewOrder(@RequestBody HandleNewOrderDto handleNewOrderDto) {
        if (handleNewOrderDto.getOrderId() == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order id can not be null"
            );
        }

        if (handleNewOrderDto.getOrderStatus() == null || handleNewOrderDto.getOrderStatus() == OrderStatus.NEW_ORDER) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "given order status is not legit"
            );
        }

        Order order = orderService.getOrderById(handleNewOrderDto.getOrderId());
        if (order == null) {
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no order found"
            );
        }

        if (order.getStatus() != OrderStatus.NEW_ORDER) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order already managed"
            );
        }

        //update order status
        order.setStatus(handleNewOrderDto.getOrderStatus());
        orderService.createOrUpdateOrder(order);
        APIResponse updateProductsResponse = null;

        //if admin denies the order , update product(s) stocks
        if (handleNewOrderDto.getOrderStatus() == OrderStatus.DENIED) {
            String token = jwtToken.getToken(SECRET_KEY).getJwtToken();
            Request updateProductsRequest = Request.Put(productServiceUri + "updateProductStockByOrderId/" + order.getId());
            updateProductsResponse = appUtil.sendRequest(updateProductsRequest, token);
        }

        String message = updateProductsResponse == null
                ? "failed"
                : updateProductsResponse.getMessage();

        return new APIResponse(
                HttpStatus.OK,
                message
        );
    }
}
