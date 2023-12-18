package com.example.commonservice.Entity.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtTokenResponse {

    private String JwtToken;

    private String message;

    public JwtTokenResponse(String message){
        this.message = message;
    }
}
