package com.fisa.notificationapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableAsync
@EnableDiscoveryClient
@SpringBootApplication
public class NotificationApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApiApplication.class, args);
    }

}
