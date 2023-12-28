package com.example.orderservice.Service;

import com.example.commonservice.Entity.APIResponse;
import com.example.commonservice.Entity.Dto.OrderDto;
import com.example.commonservice.Entity.Dto.ProductDto;
import com.example.commonservice.Entity.Dto.UserDetailsDto;
import com.example.commonservice.Entity.Enum.OrderStatus;
import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.AppUtil;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JwtToken;
import com.example.orderservice.Entity.Dto.CreateOrderDto;
import com.example.orderservice.Entity.Dto.HandleNewOrderDto;
import com.example.orderservice.Entity.Order;
import com.example.orderservice.Repository.OrderRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class OrderService {
    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final String productServiceUri = "http://localhost:9000/product-service/api/product/";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private JwtToken jwtToken;

    @Autowired
    private AppUtil appUtil;

    @Autowired
    private CustomMapper customMapper;


    public APIResponse createOrder(CreateOrderDto createOrderDto) {
        UserDetailsDto loggedInUser = commonService.getLoggedInUser();
        UUID loggedInUserId = loggedInUser.getId();
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
        URI uri = buildUri(productIdMap, productIdList);

        if(uri == null){
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "failed"
            );
        }


        //get product list
        logger.info("getting products");
        Request getProductsRequest = Request.Get(uri);
        APIResponse getProductsResponse = appUtil.sendRequest(getProductsRequest, token);
        if(getProductsResponse == null){
            logger.info("get products failed");
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "get products failed"
            );
        }

        if (getProductsResponse.getHttpStatus() != HttpStatus.OK) {
            logger.info("no product found for: {}", productIdList);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no product found"
            );
        }


        //convert api result into List<ProductDto>
        List<ProductDto> productDtoList;
        try{
            Gson gson = new Gson();
            Object resultObject = getProductsResponse.getResult();
            Type listType = new TypeToken<List<ProductDto>>() {}.getType();

            String json;
            //if there is only one product
            if (productIdMap.size() == 1) {
                json = gson.toJson(List.of(resultObject));
            } else {
                json = gson.toJson(resultObject);
            }

            productDtoList = gson.fromJson(json, listType);

        } catch (Exception e){
            logger.error("error converting api result into ProductDto List: {}", e.getMessage(), e);
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "failed"
            );
        }


        //check if products exist and in stock and have enough stock
        logger.info("validating products");
        APIResponse validateProductsResponse = validateProducts(productDtoList, productIdMap);

        boolean isResponseNotSuccess = !Objects.equals(validateProductsResponse.getMessage(), "success");
        if(isResponseNotSuccess){
            logger.info("${validateProductsResponse.getMessage()}, {}", validateProductsResponse.getResult());
            return validateProductsResponse;
        }


        //create order
        logger.info("creating order");
        Order order = customMapper.map(createOrderDto, Order.class);
        logger.info("validating products");
        Long totalPrice = (Long) validateProductsResponse.getResult();
        order.setTotalPrice(totalPrice);

        Order createdOrder = orderRepository.save(order);
        logger.info("created order with id: {}",createdOrder.getId());

        //update product(s) stock(s)
        logger.info("updating product(s)");
        Request updateProductsRequest = Request.Put(productServiceUri + "updateProductStockByOrderId/" + createdOrder.getId());
        APIResponse updateProductsResponse = appUtil.sendRequest(updateProductsRequest, token);

        if(updateProductsResponse == null){
            logger.info("update products failed");
            return new APIResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "failed"
            );
        }

        logger.info("create order success");
        return new APIResponse(
                HttpStatus.OK,
                updateProductsResponse.getMessage()
        );
    }


    public APIResponse handleNewOrder(HandleNewOrderDto handleNewOrderDto){
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

        logger.info("getting order with id: {}",handleNewOrderDto.getOrderId());
        Order order = getById(handleNewOrderDto.getOrderId());
        if (order == null) {
            logger.info("no order found with id: {}", handleNewOrderDto.getOrderId());
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no order found"
            );
        }

        if (order.getStatus() != OrderStatus.NEW_ORDER) {
            logger.info("order already managed: {}", order.getStatus());
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order already managed"
            );
        }

        //update order status
        logger.info("updating order status");
        order.setStatus(handleNewOrderDto.getOrderStatus());
        orderRepository.save(order);


        //if admin denies the order , update product(s) stocks
        APIResponse updateProductsResponse = new APIResponse();

        if (handleNewOrderDto.getOrderStatus() == OrderStatus.DENIED) {
            logger.info("updating products");
            String token = jwtToken.getToken(SECRET_KEY).getJwtToken();

            Request updateProductsRequest = Request.Put(productServiceUri + "updateProductStockByOrderId/" + order.getId());

            updateProductsResponse = appUtil.sendRequest(updateProductsRequest, token);
        }

        //manage result message
        String message;
        if (updateProductsResponse == null){
            logger.info("updating products failed");
            message = "update products failed";
        }else if (updateProductsResponse.getHttpStatus() == null){
            //that means admin approved order
            message = "success";
        } else {
            message = updateProductsResponse.getMessage();
        }

        logger.info("handleNewOrder success");
        return new APIResponse(
                HttpStatus.OK,
                message
        );
    }

    public APIResponse getOrderById(UUID id) {
        if (id == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "order id can not be null"
            );
        }

        logger.info("getting order with id: {}",id);
        Order order = getById(id);
        if (order == null) {
            logger.info("no order found with id: {}",id);
            return new APIResponse(
                    HttpStatus.NOT_FOUND,
                    "no order found"
            );
        }

        OrderDto orderDto = customMapper.map(order, OrderDto.class);
        logger.info("get order success");
        return new APIResponse(
                HttpStatus.OK,
                "success",
                orderDto
        );
    }


    public APIResponse getOrdersByUserId(UUID userId) {
        if (userId == null) {
            return new APIResponse(
                    HttpStatus.BAD_REQUEST,
                    "user id can not be null"
            );
        }

        logger.info("getting order with userId: {}",userId);
        List<Order> orderList = getByUserId(userId);
        if (orderList.isEmpty()) {
            logger.info("no order found with userId: {}", userId);
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    orderList
            );
        }

        List<OrderDto> orderDtoList = customMapper.convertList(orderList, OrderDto.class);
        logger.info("getOrdersByUserId success");
        return new APIResponse(
                HttpStatus.OK,
                "success",
                orderDtoList
        );
    }

    public APIResponse getOrders() {
        logger.info("getting orders");
        List<Order> orderList = orderRepository.findAll();
        if (orderList.isEmpty()) {
            logger.info("no order found");
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    orderList
            );
        }

        List<OrderDto> orderDtoList = customMapper.convertList(orderList, OrderDto.class);
        logger.info("getOrders success");
        return new APIResponse(
                HttpStatus.OK,
                "success",
                orderDtoList
        );
    }


    public APIResponse getNewOrders() {
        logger.info("getting new orders");
        List<Order> newOrderList = orderRepository.findOrdersByStatus(OrderStatus.NEW_ORDER);
        if (newOrderList.isEmpty()) {
            logger.info("no new order found");
            return new APIResponse(
                    HttpStatus.OK,
                    "success",
                    newOrderList
            );
        }

        List<OrderDto> newOrderDtoList = customMapper.convertList(newOrderList, OrderDto.class);
        logger.info("getNewOrders success");
        return new APIResponse(
                HttpStatus.OK,
                "success",
                newOrderDtoList
        );
    }


    private URI buildUri(Map<UUID, Integer> productIdMap, List<UUID> productIdList) {
        URI uri;
        try {
            //if there is only one product
            logger.info("building uri for productService");
            if (productIdMap.size() == 1) {
                URIBuilder uriBuilder = new URIBuilder(productServiceUri + "getProductById/" + productIdList.stream().findFirst().orElse(null));

                uri = uriBuilder.build();

            //if there is more than one product
            } else {
                URIBuilder uriBuilder = new URIBuilder(productServiceUri + "getProducts");

                for (UUID productId : productIdList) {
                    uriBuilder.addParameter("id", String.valueOf(productId));
                }

                uri = uriBuilder.build();
            }

            logger.info("built uri: {}", uri);
            return uri;

        } catch (URISyntaxException e) {
            logger.error("building uri failed: {}", e.getMessage(), e);
            return null;
        }
    }


    private APIResponse validateProducts(List<ProductDto> productDtoList, Map<UUID, Integer> productIdMap){
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

            totalPrice += productDto.getPrice() * quantity.longValue();

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

        return new APIResponse(
                HttpStatus.OK,
                "success",
                totalPrice
        );
    }


    private List<Order> getByUserId(UUID userId){
        return orderRepository.findOrdersByUserId(userId);
    }


    private Order getById(UUID id){
        return orderRepository.findById(id).orElse(null);
    }
}
