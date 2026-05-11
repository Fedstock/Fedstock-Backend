package com.fedstock.backend.product.application;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fedstock.backend.main.error.BadRequestException;
import com.fedstock.backend.product.infrastructure.InventoryEntity;
import com.fedstock.backend.product.infrastructure.InventoryJpaRepository;
import com.fedstock.backend.product.infrastructure.ProductEntity;
import com.fedstock.backend.product.infrastructure.ProductJpaRepository;
import com.fedstock.backend.store.application.StoreService;
import com.fedstock.backend.store.infrastructure.StoreEntity;

@Service
public class ProductService {

    private final ProductJpaRepository productRepository;
    private final InventoryJpaRepository inventoryRepository;
    private final StoreService storeService;

    public ProductService(
        ProductJpaRepository productRepository,
        InventoryJpaRepository inventoryRepository,
        StoreService storeService
    ) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.storeService = storeService;
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> findAll(Long userId, Long storeId, boolean activeOnly) {
        storeService.requireMember(userId, storeId);
        if (activeOnly) {
            return productRepository.findByStore_IdAndActiveTrueOrderByCreatedAtDesc(storeId);
        }
        return productRepository.findByStore_IdOrderByCreatedAtDesc(storeId);
    }

    @Transactional
    public ProductEntity create(
        Long userId,
        Long storeId,
        String name,
        String category,
        String unit,
        Integer safetyStock,
        Integer quantity
    ) {
        StoreEntity store = storeService.requireMember(userId, storeId).getStore();
        ProductEntity product = productRepository.save(new ProductEntity(
            store,
            requireName(name),
            blankToNull(category),
            defaultUnit(unit),
            defaultSafetyStock(safetyStock)
        ));

        if (quantity != null) {
            inventoryRepository.save(new InventoryEntity(product, requireNonNegative(quantity, "quantity")));
        }

        return findById(userId, storeId, product.getId());
    }

    @Transactional(readOnly = true)
    public ProductEntity findById(Long userId, Long storeId, Long productId) {
        storeService.requireMember(userId, storeId);
        return requireProduct(storeId, productId);
    }

    @Transactional
    public ProductEntity update(
        Long userId,
        Long storeId,
        Long productId,
        String name,
        String category,
        String unit,
        Integer safetyStock,
        Boolean active
    ) {
        storeService.requireMember(userId, storeId);
        ProductEntity product = requireProduct(storeId, productId);
        product.update(
            name == null ? null : requireName(name),
            blankToNull(category),
            unit == null ? null : defaultUnit(unit),
            safetyStock == null ? null : requireNonNegative(safetyStock, "safetyStock"),
            active
        );

        return product;
    }

    @Transactional
    public InventoryEntity upsertInventory(Long userId, Long storeId, Long productId, Integer quantity) {
        storeService.requireMember(userId, storeId);
        ProductEntity product = requireProduct(storeId, productId);
        Integer normalizedQuantity = requireNonNegative(quantity, "quantity");

        InventoryEntity inventory = inventoryRepository.findByProduct_Id(productId)
            .orElseGet(() -> new InventoryEntity(product, normalizedQuantity));
        inventory.updateQuantity(normalizedQuantity);

        return inventoryRepository.save(inventory);
    }

    public ProductEntity requireProduct(Long storeId, Long productId) {
        return productRepository.findByIdAndStore_Id(productId, storeId)
            .orElseThrow(() -> new NoSuchElementException("Product not found."));
    }

    private String requireName(String name) {
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new BadRequestException("Product name must be 1 to 100 characters.");
        }
        return name;
    }

    private String defaultUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return "EA";
        }
        if (unit.length() > 30) {
            throw new BadRequestException("Product unit must be 30 characters or less.");
        }
        return unit;
    }

    private Integer defaultSafetyStock(Integer safetyStock) {
        return safetyStock == null ? 0 : requireNonNegative(safetyStock, "safetyStock");
    }

    private Integer requireNonNegative(Integer value, String fieldName) {
        if (value == null || value < 0) {
            throw new BadRequestException(fieldName + " must be 0 or greater.");
        }
        return value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
