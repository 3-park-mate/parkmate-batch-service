package com.parkmate.batchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
public class BatchserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchserviceApplication.class, args);
	}

}
