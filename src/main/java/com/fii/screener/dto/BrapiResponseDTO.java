package com.fii.screener.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BrapiResponseDTO(
        List<BrapiFiiDTO> fiis
) {
}
