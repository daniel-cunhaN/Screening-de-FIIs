package com.fii.screener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dim_fiis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimFii {

    @Id
    @Column(name = "ticker", nullable = false, unique = true, length = 10)
    private String ticker;

    @Column(name = "segment")
    private String segment;

    @Column(name = "tipo_fundo")
    private String tipoFundo;

    @Column(name = "nome_fundo")
    private String nomeFundo;
}
