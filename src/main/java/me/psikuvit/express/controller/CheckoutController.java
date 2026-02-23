package me.psikuvit.express.controller;

import jakarta.validation.Valid;
import me.psikuvit.express.dto.CheckoutRequest;
import me.psikuvit.express.dto.CheckoutResponse;
import me.psikuvit.express.dto.PriceCalculationRequest;
import me.psikuvit.express.dto.PriceCalculationResponse;
import me.psikuvit.express.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        CheckoutResponse response = checkoutService.processCheckout(request, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/calc")
    public ResponseEntity<PriceCalculationResponse> calculatePrice(@Valid @RequestBody PriceCalculationRequest request) {
        PriceCalculationResponse response = checkoutService.calculatePrice(
                request.getProducts(),
                request.getUserLocation()
        );
        return ResponseEntity.ok(response);
    }
}

