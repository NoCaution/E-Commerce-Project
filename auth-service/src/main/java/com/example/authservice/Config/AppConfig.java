package com.example.authservice.Config;

import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public JWTUtil jwtUtil(){
        return new JWTUtil();
    }

    @Bean
    public CommonService commonService(){
        return new CommonService();
    }
}
