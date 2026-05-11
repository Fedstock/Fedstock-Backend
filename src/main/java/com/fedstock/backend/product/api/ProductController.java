package com.fedstock.backend.product.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fedstock.backend.auth.api.CurrentUser;
import com.fedstock.backend.auth.application.UserPrincipal;
import com.fedstock.backend.product.api.dto.CreateProductRequest;
import com.fedstock.backend.product.api.dto.InventoryResponse;
import com.fedstock.backend.product.api.dto.InventoryUpsertRequest;
import com.fedstock.backend.product.api.dto.ProductResponse;
import com.fedstock.backend.product.api.dto.UpdateProductRequest;
import com.fedstock.backend.product.application.ProductService;

@RestController
@RequestMapping("/api/stores/{storeId}/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> findAll(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @RequestParam(required = false) Boolean activeOnly,
        @RequestParam(name = "is_active", required = false) Boolean activeAlias
    ) {
        boolean onlyActive = activeOnly == null ? activeAlias == null || activeAlias : activeOnly;
        return productService.findAll(user.id(), storeId, onlyActive)
            .stream()
            .map(ProductResponse::from)
            .toList();
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResponse response = ProductResponse.from(productService.create(
            user.id(),
            storeId,
            request.name(),
            request.category(),
            request.unit(),
            request.safetyStock(),
            request.quantity()
        ));
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{productId}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{productId}")
    public ProductResponse findById(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @PathVariable Long productId
    ) {
        return ProductResponse.from(productService.findById(user.id(), storeId, productId));
    }

    @PatchMapping("/{productId}")
    public ProductResponse update(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @PathVariable Long productId,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        return ProductResponse.from(productService.update(
            user.id(),
            storeId,
            productId,
            request.name(),
            request.category(),
            request.unit(),
            request.safetyStock(),
            request.isActive()
        ));
    }

    @PutMapping("/{productId}/inventory")
    public InventoryResponse upsertInventory(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @PathVariable Long productId,
        @Valid @RequestBody InventoryUpsertRequest request
    ) {
        return InventoryResponse.from(productService.upsertInventory(user.id(), storeId, productId, request.quantity()));
    }
}
