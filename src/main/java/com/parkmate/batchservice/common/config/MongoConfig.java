package com.parkmate.batchservice.common.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoRepositories(basePackages = "com.parkmate.batchservice.reviewsummary.infrastructure.repository")
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString("mongodb+srv://kbm8527:admin1234@clusterchat.puzhiis.mongodb.net/reviewsummary?retryWrites=true&w=majority&appName=Clusterchat");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(10, TimeUnit.SECONDS)
                )
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "reviewsummary");
    }
}