package com.example.authservice.Entity.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDto {

    private String token;

    private boolean userExists = true;

    public AuthenticationResponseDto(boolean userExists){
        this.userExists = userExists;
    }

    public AuthenticationResponseDto(String token){
        this.token = token;
    }
}
