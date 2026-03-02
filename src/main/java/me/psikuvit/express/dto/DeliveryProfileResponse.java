package me.psikuvit.express.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.psikuvit.express.model.Location;

@Data
@AllArgsConstructor
public class DeliveryProfileResponse {
    private Long id;
    private String name;
    private Integer age;
    private String car;
    private String whatsappNumber;
    private Location location;
    private Boolean available;
    private boolean registered;
}
