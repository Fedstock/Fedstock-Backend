package com.fedstock.backend.admin.s3.api.dto;

import java.time.Instant;

public record S3ObjectResponse(
    String key,
    Long size,
    Instant lastModified,
    String eTag,
    String storageClass
) {
}
