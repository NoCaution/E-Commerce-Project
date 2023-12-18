package com.example.commonservice.Util;

import com.example.commonservice.Entity.Dto.JwtTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JwtToken {

    @Value("${application.security.jwt.secret-key}")
    private String SECRET_KEY;

    private String jwtToken;

    public JwtTokenResponse getToken(String SECRET_KEY) {
        if (Objects.equals(SECRET_KEY, this.SECRET_KEY)) {
            return new JwtTokenResponse(
                    jwtToken,
                    "sucess"
            );
        } else {
            return new JwtTokenResponse(
                    "invalid key"
            );
        }
    }

    public void setToken(String SECRET_KEY, String jwtToken) {
        if (Objects.equals(SECRET_KEY, this.SECRET_KEY)) {
            this.jwtToken = jwtToken;
        }
    }
}