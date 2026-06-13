package com.fedstock.backend.admin.s3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
public record S3AdminProperties(
    String bucket,
    String region,
    String adminPassword
) {
}
