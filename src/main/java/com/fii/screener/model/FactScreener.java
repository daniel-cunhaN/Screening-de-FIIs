package com.fii.screener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "fact_screener")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactScreener {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticker", nullable = false)
    private DimFii fii;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "preco_atual")
    private Double precoAtual;

    @Column(name = "dividend_yield")
    private Double dividendYield;

    @Column(name = "ultimo_dividendo")
    private Double ultimoDividendo;

    @Column(name = "vacancia")
    private Double vacancia;

    @Column(name = "liquidez_diaria")
    private Double liquidezDiaria;

    @Column(name = "pvp")
    private Double pvp;
}
