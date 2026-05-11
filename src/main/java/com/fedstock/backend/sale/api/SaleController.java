package com.fedstock.backend.sale.api;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fedstock.backend.auth.api.CurrentUser;
import com.fedstock.backend.auth.application.UserPrincipal;
import com.fedstock.backend.sale.api.dto.CreateSaleRequest;
import com.fedstock.backend.sale.api.dto.CreatedSaleResponse;
import com.fedstock.backend.sale.api.dto.SaleResponse;
import com.fedstock.backend.sale.application.SaleService;

@RestController
@RequestMapping("/api/stores/{storeId}/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<CreatedSaleResponse> create(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @Valid @RequestBody CreateSaleRequest request
    ) {
        CreatedSaleResponse response = CreatedSaleResponse.from(saleService.create(
            user.id(),
            storeId,
            request.productId(),
            request.soldQuantity(),
            request.soldAt()
        ));
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{saleId}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<SaleResponse> search(
        @CurrentUser UserPrincipal user,
        @PathVariable Long storeId,
        @RequestParam(required = false) Long productId,
        @RequestParam(name = "product_id", required = false) Long productIdAlias,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(required = false) Integer limit
    ) {
        Long selectedProductId = productId == null ? productIdAlias : productId;
        return saleService.search(user.id(), storeId, selectedProductId, from, to, limit)
            .stream()
            .map(SaleResponse::from)
            .toList();
    }
}
