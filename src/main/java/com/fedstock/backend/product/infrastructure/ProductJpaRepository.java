package com.fedstock.backend.product.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    @EntityGraph(attributePaths = {"inventory"})
    List<ProductEntity> findByStore_IdAndActiveTrueOrderByCreatedAtDesc(Long storeId);

    @EntityGraph(attributePaths = {"inventory"})
    List<ProductEntity> findByStore_IdOrderByCreatedAtDesc(Long storeId);

    @EntityGraph(attributePaths = {"inventory"})
    Optional<ProductEntity> findByIdAndStore_Id(Long productId, Long storeId);
}
