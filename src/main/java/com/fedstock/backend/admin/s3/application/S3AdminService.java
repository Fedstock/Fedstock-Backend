package com.fedstock.backend.admin.s3.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fedstock.backend.admin.s3.api.dto.S3DeleteAllResponse;
import com.fedstock.backend.admin.s3.api.dto.S3ObjectResponse;
import com.fedstock.backend.admin.s3.api.dto.S3ObjectsResponse;
import com.fedstock.backend.admin.s3.config.S3AdminProperties;
import com.fedstock.backend.main.error.BadGatewayException;
import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.main.error.ForbiddenException;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteMarkerEntry;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.ObjectVersion;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
public class S3AdminService {

    private static final int DELETE_BATCH_SIZE = 1000;

    private final S3Client s3Client;
    private final S3AdminProperties properties;

    public S3AdminService(S3Client s3Client, S3AdminProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    public S3ObjectsResponse listObjects(String password) {
        validatePassword(password);

        List<S3ObjectResponse> objects = new ArrayList<>();
        long totalBytes = 0;

        try {
            for (S3Object object : s3Client.listObjectsV2Paginator(request -> request.bucket(bucket())).contents()) {
                totalBytes += object.size();
                objects.add(new S3ObjectResponse(
                    object.key(),
                    object.size(),
                    object.lastModified(),
                    object.eTag(),
                    object.storageClassAsString()
                ));
            }
        } catch (AwsServiceException | SdkClientException exception) {
            throw new BadGatewayException("Failed to read S3 bucket.");
        }

        return new S3ObjectsResponse(bucket(), objects.size(), totalBytes, objects);
    }

    public S3DeleteAllResponse deleteAllObjects(String password) {
        validatePassword(password);

        long deletedObjectCount = 0;
        long deletedBytes = 0;
        List<ObjectIdentifier> batch = new ArrayList<>(DELETE_BATCH_SIZE);

        try {
            for (ObjectVersion object : s3Client.listObjectVersionsPaginator(request -> request.bucket(bucket())).versions()) {
                batch.add(objectIdentifier(object.key(), object.versionId()));
                deletedObjectCount++;
                deletedBytes += object.size();

                if (batch.size() == DELETE_BATCH_SIZE) {
                    deleteBatch(batch);
                    batch.clear();
                }
            }

            for (DeleteMarkerEntry marker : s3Client.listObjectVersionsPaginator(request -> request.bucket(bucket())).deleteMarkers()) {
                batch.add(objectIdentifier(marker.key(), marker.versionId()));
                deletedObjectCount++;

                if (batch.size() == DELETE_BATCH_SIZE) {
                    deleteBatch(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                deleteBatch(batch);
            }
        } catch (AwsServiceException | SdkClientException exception) {
            throw new BadGatewayException("Failed to delete S3 bucket objects.");
        }

        return new S3DeleteAllResponse(bucket(), deletedObjectCount, deletedBytes);
    }

    private void deleteBatch(List<ObjectIdentifier> objects) {
        Delete delete = Delete.builder()
            .objects(List.copyOf(objects))
            .quiet(true)
            .build();

        s3Client.deleteObjects(DeleteObjectsRequest.builder()
            .bucket(bucket())
            .delete(delete)
            .build());
    }

    private ObjectIdentifier objectIdentifier(String key, String versionId) {
        ObjectIdentifier.Builder builder = ObjectIdentifier.builder().key(key);
        if (versionId != null && !versionId.isBlank()) {
            builder.versionId(versionId);
        }
        return builder.build();
    }

    private void validatePassword(String password) {
        String expectedPassword = properties.adminPassword();
        if (expectedPassword == null || expectedPassword.isBlank()) {
            throw new BadRequestException("S3 admin password is not configured.");
        }

        byte[] expected = expectedPassword.getBytes(StandardCharsets.UTF_8);
        byte[] actual = password == null ? new byte[0] : password.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new ForbiddenException("S3 admin password is invalid.");
        }
    }

    private String bucket() {
        String bucket = properties.bucket();
        if (bucket == null || bucket.isBlank()) {
            throw new BadRequestException("S3 bucket is not configured.");
        }
        return bucket;
    }
}
