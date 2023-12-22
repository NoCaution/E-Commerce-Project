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

    private boolean emailPasswordIncorrect = true;

    public AuthenticationResponseDto(boolean userExists){
        this.userExists = userExists;
    }

    public AuthenticationResponseDto(String token, boolean userExists){
        this.token = token;
        this.userExists = userExists;
    }
}
