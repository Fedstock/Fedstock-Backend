package com.fedstock.backend.admin.s3.api.dto;

public record S3DeleteAllResponse(
    String bucket,
    long deletedObjectCount,
    long deletedBytes
) {
}
