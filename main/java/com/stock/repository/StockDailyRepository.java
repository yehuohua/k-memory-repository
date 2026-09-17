package com.stock.repository;

import com.stock.entity.StockDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockDailyRepository extends JpaRepository<StockDaily, Long> {

    List<StockDaily> findByCodeOrderByTradeDateAsc(String code);

    Optional<StockDaily> findFirstByCodeOrderByTradeDateDesc(String code);

    long countByCode(String code);

    void deleteByCode(String code);
}
