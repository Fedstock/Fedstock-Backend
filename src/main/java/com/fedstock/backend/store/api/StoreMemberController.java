package com.fedstock.backend.store.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fedstock.backend.auth.api.CurrentUser;
import com.fedstock.backend.auth.application.UserPrincipal;
import com.fedstock.backend.store.api.dto.AddStoreMemberRequest;
import com.fedstock.backend.store.api.dto.AddStoreMemberResponse;
import com.fedstock.backend.store.api.dto.StoreMemberResponse;
import com.fedstock.backend.store.application.StoreService;

@RestController
@RequestMapping("/api/stores/{storeId}/members")
public class StoreMemberController {

    private final StoreService storeService;

    public StoreMemberController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public List<StoreMemberResponse> findMembers(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId
    ) {
        return storeService.findMembers(user.id(), storeId)
            .stream()
            .map(StoreMemberResponse::from)
            .toList();
    }

    @PostMapping
    public ResponseEntity<AddStoreMemberResponse> addMember(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @Valid @RequestBody AddStoreMemberRequest request
    ) {
        AddStoreMemberResponse response = AddStoreMemberResponse.from(
            storeService.addMember(user.id(), storeId, request.email(), request.role())
        );
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{memberId}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
