package me.psikuvit.express.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationRequest {
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private String address;
}

