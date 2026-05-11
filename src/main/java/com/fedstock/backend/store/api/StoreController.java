package com.fedstock.backend.store.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fedstock.backend.auth.api.CurrentUser;
import com.fedstock.backend.auth.application.UserPrincipal;
import com.fedstock.backend.store.api.dto.CreateStoreRequest;
import com.fedstock.backend.store.api.dto.StoreResponse;
import com.fedstock.backend.store.api.dto.UpdateStoreRequest;
import com.fedstock.backend.store.application.StoreService;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public List<StoreResponse> findMyStores(@CurrentUser UserPrincipal user) {
        return storeService.findMyStores(user.id())
            .stream()
            .map(StoreResponse::from)
            .toList();
    }

    @PostMapping
    public ResponseEntity<StoreResponse> create(
        @CurrentUser UserPrincipal user,
        @Valid @RequestBody CreateStoreRequest request
    ) {
        StoreResponse response = StoreResponse.from(storeService.create(user.id(), request.name(), request.businessType()));
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{storeId}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{storeId}")
    public StoreResponse findById(@CurrentUser UserPrincipal user, @PathVariable Long storeId) {
        return StoreResponse.from(storeService.findMyStore(user.id(), storeId));
    }

    @PatchMapping("/{storeId}")
    public StoreResponse update(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @Valid @RequestBody UpdateStoreRequest request
    ) {
        return StoreResponse.from(storeService.update(user.id(), storeId, request.name(), request.businessType()));
    }
}
