package com.example.api.gateway.Security;

import com.example.api.gateway.Entity.UserDetailsDto;
import com.example.api.gateway.service.UserService;
import com.example.api.gateway.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;


import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class CustomGateWayFilter extends AbstractGatewayFilterFactory<CustomGateWayFilter.Config> {
    private final Logger logger = LoggerFactory.getLogger(CustomGateWayFilter.class);

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserService userService;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> WHITE_LIST = List.of(
            "/auth-service/v3/api-docs/**",
            "/product-service/v3/api/docs/**",
            "/user-service/v3/api/docs/**",
            "/basket-service/v3/api/docs/**",
            "/order-service/v3/api/docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/auth-service/api/auth/**"
    );

    public CustomGateWayFilter() {
        super(Config.class);
    }

    private boolean isInWhiteList(String requestPath) {
        return WHITE_LIST.stream()
                .anyMatch(path -> pathMatcher.match(path,requestPath));
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = exchange.getRequest().getURI().getPath();

            //is uri in WHITE_LIST
            if (isInWhiteList(requestPath)) {
                return chain.filter(exchange);
            }

            //if auth header is missing
            if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                logger.error("authorization header was missing");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            //get header and validate
            List<String> authorizationHeaders = exchange.getRequest().getHeaders().get("Authorization");

            if (authorizationHeaders == null || !authorizationHeaders.get(0).startsWith("Bearer ")) {
                logger.error("authorization header invalid");
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }

            String authorizationHeader = authorizationHeaders.get(0);
            String token;

            //get token
            token = authorizationHeader.substring(7).trim();
            String id = jwtUtil.extractUserId(token);

            //get user and create request
            if (id != null) {
                String headerValue;

                boolean isNotGetLoggedInUserRequest = !Objects.equals(exchange.getRequest().getURI().getPath(), "/user-service/api/user/getLoggedInUser");
                if (isNotGetLoggedInUserRequest) {
                    UserDetailsDto user = userService.sendRequest(token);

                    User userDetails = new User(
                            user.getId().toString(),
                            user.getPassword(),
                            Set.of(new SimpleGrantedAuthority(user.getRole().toString()))
                    );

                    boolean isTokenValid = jwtUtil.isTokenValid(token, userDetails);
                    if (isTokenValid) {
                        String userName = userDetails.getUsername();
                        String password = user.getPassword();
                        String role = userDetails.getAuthorities().stream().toList().get(0).getAuthority();

                        //add user credentials to header
                        headerValue = String.join(" ", token, userName, password, role);
                        request = exchange.getRequest().mutate().header("CustomAuthorization", headerValue).build();
                    }

                    //for request to /user-service/api/user/getLoggedInUser , token is enough to send
                } else {
                    headerValue = token;
                    request = exchange.getRequest().mutate().header("CustomAuthorization", headerValue).build();
                }
            }

            return chain.filter(exchange.mutate().request(request).build());
        });
    }

    public static class Config {
    }
}
