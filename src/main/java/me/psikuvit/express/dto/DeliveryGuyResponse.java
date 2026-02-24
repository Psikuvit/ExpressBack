package me.psikuvit.express.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.psikuvit.express.model.Location;

@Data
@AllArgsConstructor
public class DeliveryGuyResponse {
    private Long id;
    private String name;
    private Integer age;
    private String car;
    private String whatsappNumber;
    private Location nearestLocation;
    private Boolean available;
    private Double distanceFromUser;
}

