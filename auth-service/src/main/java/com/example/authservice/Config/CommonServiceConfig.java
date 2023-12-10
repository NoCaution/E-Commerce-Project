package com.example.authservice.Config;

import com.example.commonservice.Util.AppUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonServiceConfig {
    @Bean
    public AppUtil appUtil(){
        return new AppUtil();
    }
}
