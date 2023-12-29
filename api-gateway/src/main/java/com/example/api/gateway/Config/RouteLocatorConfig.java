package com.example.api.gateway.Config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class RouteLocatorConfig {
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(r -> r.path("/auth-service/v3/api-docs").and().method(HttpMethod.GET).uri("lb://auth-service"))
                .route(r -> r.path("/user-service/v3/api-docs").and().method(HttpMethod.GET).uri("lb://user-service"))
                .route(r -> r.path("/product-service/v3/api-docs").and().method(HttpMethod.GET).uri("lb://product-service"))
                .route(r -> r.path("/basket-service/v3/api-docs").and().method(HttpMethod.GET).uri("lb://basket-service"))
                .route(r -> r.path("/order-service/v3/api-docs").and().method(HttpMethod.GET).uri("lb://order-service"))
                .build();
    }
}
