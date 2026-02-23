package me.psikuvit.express.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.psikuvit.express.model.Location;

@Data
@AllArgsConstructor
public class LocationResponse {
    private Location location;
    private String message;
}

