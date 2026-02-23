package me.psikuvit.express.controller;

import me.psikuvit.express.dto.DeliveryGuyResponse;
import me.psikuvit.express.model.Location;
import me.psikuvit.express.service.DeliveryGuyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveryguys")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeliveryGuyController {

    private final DeliveryGuyService deliveryGuyService;

    public DeliveryGuyController(DeliveryGuyService deliveryGuyService) {
        this.deliveryGuyService = deliveryGuyService;
    }

    @GetMapping
    public ResponseEntity<List<DeliveryGuyResponse>> getAllDeliveryGuys(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        Location userLocation = null;
        if (latitude != null && longitude != null) {
            userLocation = new Location(latitude, longitude, null);
        }

        List<DeliveryGuyResponse> deliveryGuys = deliveryGuyService.getAllDeliveryGuys(userLocation);
        return ResponseEntity.ok(deliveryGuys);
    }
}

