package com.fedstock.backend.admin.s3.api.dto;

import java.util.List;

public record S3ObjectsResponse(
    String bucket,
    long objectCount,
    long totalBytes,
    List<S3ObjectResponse> objects
) {
}
