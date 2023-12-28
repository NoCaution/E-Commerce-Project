package com.example.commonservice.Util;

import com.example.commonservice.Entity.APIResponse;
import com.google.gson.Gson;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppUtil {

    @Autowired
    private Gson gson;

    @Autowired
    private CustomMapper customMapper;

    private final Logger logger = LoggerFactory.getLogger(AppUtil.class);

    public <T> T sendRequest(Request request, String token, Class<T> targetClass) {
        String headerValue = "Bearer " + token;

        try {
            logger.info("sending request : {}",request);
            String json = request.setHeader("Authorization", headerValue).execute().returnContent().asString();
            APIResponse apiResponse = gson.fromJson(json, APIResponse.class);

            return customMapper.map(apiResponse.getResult(), targetClass);

        } catch (IOException e) {
            logger.error("error while sending request: {}, {}",request, e.getMessage(), e);
            return null;
        }
    }

    public APIResponse sendRequest(Request request, String token) {
        String headerValue = "Bearer " + token;

        try {
            logger.info("sending request : {}",request);
            String json = request.setHeader("Authorization", headerValue).execute().returnContent().asString();
            return gson.fromJson(json, APIResponse.class);

        } catch (IOException e) {
            logger.error("error while sending request: {}, {}",request, e.getMessage(), e);
            return null;
        }
    }
}
