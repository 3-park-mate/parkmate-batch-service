package com.parkmate.batchservice;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients(basePackages = "com.parkmate.batchservice.hostsettlement.infrastructure.client")
@EnableBatchProcessing
@EnableScheduling
@EnableDiscoveryClient
@SpringBootApplication
public class BatchserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchserviceApplication.class, args);
	}

}
