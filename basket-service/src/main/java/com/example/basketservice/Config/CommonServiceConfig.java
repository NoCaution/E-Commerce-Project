package com.example.basketservice.Config;

import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.AppUtil;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JWTUtil;
import com.example.commonservice.Util.JwtToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonServiceConfig {
    @Bean
    public CommonService commonService(){
        return new CommonService();
    }

    @Bean
    public CustomMapper customMapper(){
        return new CustomMapper();
    }

    @Bean
    public AppUtil appUtil(){
        return new AppUtil();
    }

    @Bean
    public JWTUtil jwtUtil(){
        return new JWTUtil();
    }

    @Bean
    public JwtToken jwtToken(){
        return new JwtToken();
    }
}
