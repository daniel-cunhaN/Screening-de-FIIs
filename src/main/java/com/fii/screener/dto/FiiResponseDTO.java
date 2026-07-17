package com.fii.screener.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class FiiResponseDTO {
    private String ticker;
    private String nomeFundo;
    private String segment;
    
    @JsonProperty("type")
    private String tipoFundo;
    
    private Double price;
    private Double dividendYield;
    private Double ultimoDividendo;
    private Double vacancy;
    private LocalDate lastUpdate;
}
