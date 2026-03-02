package me.psikuvit.express.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.psikuvit.express.model.Location;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DeliveryOrderResponse {
    private Long orderId;
    private String customerName;
    private List<OrderItemDetail> items;
    private Double totalPrice;
    private Double distance;
    private Location deliveryLocation;
    private String status;
    private LocalDateTime createdAt;

    @Data
    @AllArgsConstructor
    public static class OrderItemDetail {
        private String productName;
        private String size;
        private Integer quantity;
        private Double price;
    }
}
