package com.fii.screener.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fiis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fii {

    @Id
    private String ticker;
    
    private Double price;
    
    private Double dividendYield; // 12m
    
    private Double pvp; // priceToNav

    private Double vacancy;

    private String segment;

    private Double netWorth;

    private Double equityValue;

    private String type;
    
    private LocalDateTime lastUpdate;
}
