package com.fedstock.backend.sale.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.product.application.ProductService;
import com.fedstock.backend.product.infrastructure.ProductEntity;
import com.fedstock.backend.sale.infrastructure.SaleEntity;
import com.fedstock.backend.sale.infrastructure.SaleJpaRepository;
import com.fedstock.backend.store.application.StoreService;

@Service
public class SaleService {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;

    private final SaleJpaRepository saleRepository;
    private final StoreService storeService;
    private final ProductService productService;

    public SaleService(
        SaleJpaRepository saleRepository,
        StoreService storeService,
        ProductService productService
    ) {
        this.saleRepository = saleRepository;
        this.storeService = storeService;
        this.productService = productService;
    }

    @Transactional
    public SaleEntity create(Long userId, Long storeId, Long productId, Integer soldQuantity, OffsetDateTime soldAt) {
        storeService.requireMember(userId, storeId);
        ProductEntity product = productService.requireProduct(storeId, productId);
        SaleEntity sale = new SaleEntity(
            product,
            requirePositive(soldQuantity, "soldQuantity"),
            soldAt == null ? null : soldAt.toLocalDateTime()
        );

        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleEntity> search(Long userId, Long storeId, Long productId, LocalDate from, LocalDate to, Integer limit) {
        storeService.requireMember(userId, storeId);

        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.plusDays(1).atStartOfDay();

        return saleRepository.search(
            storeId,
            productId,
            fromDateTime,
            toDateTime,
            PageRequest.of(0, normalizedLimit(limit))
        );
    }

    private Integer requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new BadRequestException(fieldName + " must be greater than 0.");
        }
        return value;
    }

    private int normalizedLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            throw new BadRequestException("limit must be greater than 0.");
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
