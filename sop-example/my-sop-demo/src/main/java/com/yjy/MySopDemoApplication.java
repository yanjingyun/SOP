package com.yjy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MySopDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MySopDemoApplication.class, args);
    }
}
