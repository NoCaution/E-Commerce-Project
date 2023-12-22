package com.example.commonservice.Util;

import com.example.commonservice.Entity.APIResponse;
import com.google.gson.Gson;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppUtil {

    @Autowired
    private Gson gson;
    @Autowired
    private CustomMapper customMapper;

    public <T> T sendRequest(Request request, String token, Class<T> targetClass) {
        try {
            String header = "Bearer " + token;
            String json = request.setHeader("Authorization", header).execute().returnContent().asString();
            APIResponse apiResponse = gson.fromJson(json, APIResponse.class);

            if (apiResponse.getResult() == null) {
                return null;
            }

            return customMapper.map(apiResponse.getResult(), targetClass);

        } catch (IOException e) {
            return null;
        }
    }

    public APIResponse sendRequest(Request request, String token) {
        try {
            String header = "Bearer " + token;
            String json = request.setHeader("Authorization", header).execute().returnContent().asString();
            return gson.fromJson(json, APIResponse.class);

        } catch (IOException e) {
            return null;
        }
    }
}
