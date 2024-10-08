package com.sm.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class AuthJwtServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthJwtServiceApplication.class, args);
	}

}
