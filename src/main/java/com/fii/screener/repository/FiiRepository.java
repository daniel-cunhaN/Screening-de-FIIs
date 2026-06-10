package com.fii.screener.repository;

import com.fii.screener.model.Fii;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FiiRepository extends JpaRepository<Fii, String> {
}
