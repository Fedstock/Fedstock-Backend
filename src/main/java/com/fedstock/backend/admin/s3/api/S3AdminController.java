package com.fedstock.backend.admin.s3.api;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fedstock.backend.admin.s3.api.dto.S3AdminPasswordRequest;
import com.fedstock.backend.admin.s3.api.dto.S3DeleteAllResponse;
import com.fedstock.backend.admin.s3.api.dto.S3ObjectsResponse;
import com.fedstock.backend.admin.s3.application.S3AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/admin/s3")
@SecurityRequirement(name = "bearerAuth")
public class S3AdminController {

    private final S3AdminService s3AdminService;

    public S3AdminController(S3AdminService s3AdminService) {
        this.s3AdminService = s3AdminService;
    }

    @Operation(summary = "List all S3 objects in the configured artifact bucket")
    @PostMapping("/objects")
    public S3ObjectsResponse listObjects(@Valid @RequestBody S3AdminPasswordRequest request) {
        return s3AdminService.listObjects(request.pw());
    }

    @Operation(summary = "Delete all S3 objects in the configured artifact bucket")
    @PostMapping("/objects/delete-all")
    public S3DeleteAllResponse deleteAllObjects(@Valid @RequestBody S3AdminPasswordRequest request) {
        return s3AdminService.deleteAllObjects(request.pw());
    }
}
