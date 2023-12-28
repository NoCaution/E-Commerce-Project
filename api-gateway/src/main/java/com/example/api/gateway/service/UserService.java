package com.example.api.gateway.service;

import com.example.api.gateway.Entity.APIResponse;
import com.example.api.gateway.Entity.UserDetailsDto;
import com.example.api.gateway.util.CustomMapper;
import com.google.gson.Gson;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class UserService {
    @Autowired
    private CustomMapper customMapper;

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final String USER_SERVICE_URL = "http://localhost:9000/user-service/api/user/getLoggedInUser";


    @Cacheable(value = "user-cache", key = "#token")
    public org.springframework.security.core.userdetails.User getUser(String token) throws UsernameNotFoundException {
        logger.info("getting user");
        UserDetailsDto userDetailsDto = sendRequest(Request.Get(USER_SERVICE_URL), token);

        return new org.springframework.security.core.userdetails.User(
                userDetailsDto.getId().toString(),
                userDetailsDto.getPassword(),
                Set.of(new SimpleGrantedAuthority(userDetailsDto.getRole().toString()))
        );
    }

    public UserDetailsDto sendRequest(Request request, String token) {
        logger.info("sending request: {}", request);
        try {
            Gson gson = new Gson();

            String headerValue = "Bearer " + token;
            String json = request.setHeader("Authorization", headerValue).execute().returnContent().asString();
            APIResponse apiResponse = gson.fromJson(json, APIResponse.class);

            logger.info("request success");
            return customMapper.map(apiResponse.getResult(), UserDetailsDto.class);

        } catch (IOException e) {
            logger.error("error while sending request: {}", e.getMessage(), e);
            return null;
        }
    }
}
