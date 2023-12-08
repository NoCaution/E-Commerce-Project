package com.example.commonservice.Util;

import com.google.gson.Gson;
import org.apache.http.client.fluent.Request;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppUtil {
    private final Gson gson = new Gson();

    public <T> T sendRequest(String uri , Class<T> targetClass){
        try {
            String json = Request.Get(uri).execute().returnContent().asString();
            return gson.fromJson(json, targetClass);
        } catch (IOException e) {
            return null;
        }
    }
}
