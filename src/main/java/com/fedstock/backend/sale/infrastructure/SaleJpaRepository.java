package com.fedstock.backend.sale.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleJpaRepository extends JpaRepository<SaleEntity, Long> {

    @Query("""
        select s
        from SaleEntity s
        join fetch s.product p
        where p.store.id = :storeId
          and (:productId is null or p.id = :productId)
          and (:fromDateTime is null or s.soldAt >= :fromDateTime)
          and (:toDateTime is null or s.soldAt < :toDateTime)
        order by s.soldAt desc, s.id desc
        """)
    List<SaleEntity> search(
        @Param("storeId") Long storeId,
        @Param("productId") Long productId,
        @Param("fromDateTime") LocalDateTime fromDateTime,
        @Param("toDateTime") LocalDateTime toDateTime,
        Pageable pageable
    );
}
