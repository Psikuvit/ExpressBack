package me.psikuvit.express.controller;

import jakarta.validation.Valid;
import me.psikuvit.express.dto.LocationRequest;
import me.psikuvit.express.dto.LocationResponse;
import me.psikuvit.express.model.Location;
import me.psikuvit.express.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public ResponseEntity<LocationResponse> updateLocation(@Valid @RequestBody LocationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Location location = new Location(
                request.getLatitude(),
                request.getLongitude(),
                request.getAddress()
        );

        Location updatedLocation = locationService.updateUserLocation(username, location);
        return ResponseEntity.ok(new LocationResponse(updatedLocation, "Location updated successfully"));
    }

    @GetMapping
    public ResponseEntity<LocationResponse> getLocation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Location location = locationService.getUserLocation(username);

        if (location == null) {
            return ResponseEntity.ok(new LocationResponse(null, "No location set for user"));
        }

        return ResponseEntity.ok(new LocationResponse(location, "Location retrieved successfully"));
    }
}

