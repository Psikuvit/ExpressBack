package me.psikuvit.express.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CheckoutResponse {
    private Long orderId;
    private String message;
    private List<CheckedOrderItem> items;
    private Double totalPrice;
    private Double distance;
    private DeliveryGuyResponse assignedDeliveryGuy;
}

