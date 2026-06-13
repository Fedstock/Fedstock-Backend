package com.fedstock.backend.admin.s3.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3AdminProperties.class)
public class S3AdminConfig {

    @Bean
    public S3Client s3Client(S3AdminProperties properties) {
        return S3Client.builder()
            .region(Region.of(properties.region()))
            .build();
    }
}
