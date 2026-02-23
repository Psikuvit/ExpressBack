package me.psikuvit.express.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.psikuvit.express.model.Product;

@Data
@AllArgsConstructor
public class CheckedOrderItem {
    private Long productId;
    private String productName;
    private Product.ProductSize size;
    private Integer quantity;
    private Double basePrice;
    private Boolean available;
}

