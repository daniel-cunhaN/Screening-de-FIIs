package com.fii.screener.model;

import jakarta.persistence.Column;
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
    @Column(name = "ticker", nullable = false, unique = true, length = 10)
    private String ticker;
    
    @Column(name = "price")
    private Double price;
    
    @Column(name = "dividend_yield")
    private Double dividendYield; // 12m

    @Column(name = "dividend_yield_1m")
    private Double dividendYield1m; // 1m
    
    @Column(name = "pvp")
    private Double pvp; // priceToNav

    @Column(name = "vacancy")
    private Double vacancy;

    @Column(name = "segment")
    private String segment;

    @Column(name = "net_worth")
    private Double netWorth;

    @Column(name = "equity_value")
    private Double equityValue;

    @Column(name = "type")
    private String type;
    
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
}
