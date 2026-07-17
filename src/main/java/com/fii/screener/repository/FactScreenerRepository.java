package com.fii.screener.repository;

import com.fii.screener.model.FactScreener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FactScreenerRepository extends JpaRepository<FactScreener, Long> {
    List<FactScreener> findByFiiTicker(String ticker);
}
