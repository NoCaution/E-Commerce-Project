package com.example.commonservice.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse {

    private HttpStatus httpStatus;
    private String message;
    private Object result;

    public APIResponse(HttpStatus httpStatus,String message){
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
