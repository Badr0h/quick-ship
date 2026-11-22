package com.quickship.servicereseau;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceReseauApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceReseauApplication.class, args);
    }
}
