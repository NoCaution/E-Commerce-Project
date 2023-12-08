package com.example.basket.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class BasketServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasketServiceApplication.class, args);
	}

}
