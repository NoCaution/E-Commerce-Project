package com.example.userservice.Config;

import com.example.commonservice.Service.CommonService;
import com.example.commonservice.Util.AppUtil;
import com.example.commonservice.Util.CustomMapper;
import com.example.commonservice.Util.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonServiceConfig {
    @Bean
    public CommonService commonService(){
        return new CommonService();
    }

    @Bean
    JWTUtil jwtUtil(){
        return new JWTUtil();
    }

    @Bean
    AppUtil appUtil(){
        return new AppUtil();
    }

    @Bean
    public CustomMapper modelMapper(){
        return new CustomMapper();
    }
}
