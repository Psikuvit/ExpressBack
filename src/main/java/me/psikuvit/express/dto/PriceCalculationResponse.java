package me.psikuvit.express.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PriceCalculationResponse {
    private Double basePrice;
    private Double sizeFee;
    private Double distanceFee;
    private Double totalPrice;
    private Double distance;
    private String breakdown;
}

