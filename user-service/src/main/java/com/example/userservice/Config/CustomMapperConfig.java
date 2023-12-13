package com.example.userservice.Config;

import com.example.commonservice.Util.CustomMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomMapperConfig {

    @Bean
    public CustomMapper modelMapper(){
        return new CustomMapper();
    }
}
