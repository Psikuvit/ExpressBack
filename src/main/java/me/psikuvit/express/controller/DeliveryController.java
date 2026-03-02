package me.psikuvit.express.controller;

import jakarta.validation.Valid;
import me.psikuvit.express.dto.DeliveryOrderResponse;
import me.psikuvit.express.dto.DeliveryProfileResponse;
import me.psikuvit.express.dto.DeliveryRegistrationRequest;
import me.psikuvit.express.dto.MessageResponse;
import me.psikuvit.express.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliveryProfileResponse> register(
            @Valid @RequestBody DeliveryRegistrationRequest request) {
        String username = getUsername();
        DeliveryProfileResponse response = deliveryService.register(username, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeliveryProfileResponse> getProfile() {
        String username = getUsername();
        DeliveryProfileResponse response = deliveryService.getProfile(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<DeliveryOrderResponse>> getAvailableOrders() {
        String username = getUsername();
        List<DeliveryOrderResponse> orders = deliveryService.getAvailableOrders(username);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/orders/{orderId}/accept")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<DeliveryOrderResponse> acceptOrder(@PathVariable Long orderId) {
        String username = getUsername();
        DeliveryOrderResponse response = deliveryService.acceptOrder(username, orderId);
        return ResponseEntity.ok(response);
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
