package com.fedstock.backend.prediction.infrastructure;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryPredictionJpaRepository extends JpaRepository<InventoryPredictionEntity, Long> {

    @Query("""
        select ip
        from InventoryPredictionEntity ip
        join fetch ip.product p
        where p.store.id = :storeId
          and p.active = true
          and ip.predictedAt = (
              select max(ip2.predictedAt)
              from InventoryPredictionEntity ip2
              where ip2.product.id = p.id
          )
        order by ip.predictedAt desc, ip.id desc
        """)
    List<InventoryPredictionEntity> findLatestByStoreId(@Param("storeId") Long storeId);

    @Query("""
        select ip
        from InventoryPredictionEntity ip
        join fetch ip.product p
        where p.store.id = :storeId
          and (:productId is null or p.id = :productId)
        order by ip.predictedAt desc, ip.id desc
        """)
    List<InventoryPredictionEntity> search(
        @Param("storeId") Long storeId,
        @Param("productId") Long productId,
        Pageable pageable
    );
}
