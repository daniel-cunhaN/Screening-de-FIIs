package com.fii.screener.repository;

import com.fii.screener.model.DimFii;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DimFiiRepository extends JpaRepository<DimFii, String> {
}
