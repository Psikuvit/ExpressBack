package me.psikuvit.express.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import me.psikuvit.express.model.Location;

import java.util.List;

@Data
public class CheckoutRequest {
    @NotEmpty
    private List<OrderItemRequest> products;

    @NotNull
    private Location deliveryLocation;
}

