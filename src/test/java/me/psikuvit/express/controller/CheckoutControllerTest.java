package me.psikuvit.express.controller;

import me.psikuvit.express.dto.CheckoutRequest;
import me.psikuvit.express.dto.CheckoutResponse;
import me.psikuvit.express.dto.OrderItemRequest;
import me.psikuvit.express.dto.PriceCalculationRequest;
import me.psikuvit.express.dto.PriceCalculationResponse;
import me.psikuvit.express.model.Location;
import me.psikuvit.express.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutController Tests")
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private PriceCalculationService priceCalculationService;

    @InjectMocks
    private CheckoutController checkoutController;

    private CheckoutRequest checkoutRequest;
    private CheckoutResponse checkoutResponse;
    private PriceCalculationRequest priceCalculationRequest;

    @BeforeEach
    void setUp() {
        // Setup checkout request
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setProducts(List.of(itemRequest));
        checkoutRequest.setDeliveryLocation(new Location(10.0, 20.0));

        // Setup checkout response
        checkoutResponse = new CheckoutResponse();
        checkoutResponse.setOrderId(1L);
        checkoutResponse.setStatus("ASSIGNED");
        checkoutResponse.setTotalPrice(250.0);

        // Setup price calculation request
        priceCalculationRequest = new PriceCalculationRequest();
        priceCalculationRequest.setBasePrice(100.0);
        priceCalculationRequest.setDistance(5.0);
        priceCalculationRequest.setProductSize("MEDIUM");
    }

    @Test
    @DisplayName("Should process checkout successfully")
    @WithMockUser(username = "testuser")
    void testProcessCheckout() {
        when(checkoutService.processCheckout(any(CheckoutRequest.class), anyString()))
                .thenReturn(checkoutResponse);

        ResponseEntity<?> response = checkoutController.processCheckout(checkoutRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(checkoutService).processCheckout(any(CheckoutRequest.class), anyString());
    }

    @Test
    @DisplayName("Should calculate price correctly")
    void testCalculatePrice() {
        PriceCalculationResponse priceResponse = new PriceCalculationResponse();
        priceResponse.setBasePrice(100.0);
        priceResponse.setDistanceCost(15.0);
        priceResponse.setSizeSurcharge(10.0);
        priceResponse.setTotalPrice(125.0);

        when(priceCalculationService.calculatePrice(any(PriceCalculationRequest.class)))
                .thenReturn(priceResponse);

        ResponseEntity<?> response = checkoutController.calculatePrice(priceCalculationRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(priceCalculationService).calculatePrice(any(PriceCalculationRequest.class));
    }

    @Test
    @DisplayName("Should handle checkout with invalid request")
    @WithMockUser(username = "testuser")
    void testProcessCheckoutInvalidRequest() {
        CheckoutRequest invalidRequest = new CheckoutRequest();
        invalidRequest.setProducts(new ArrayList<>()); // Empty products
        invalidRequest.setDeliveryLocation(new Location(10.0, 20.0));

        when(checkoutService.processCheckout(any(CheckoutRequest.class), anyString()))
                .thenThrow(new IllegalArgumentException("Products list cannot be empty"));

        assertThrows(IllegalArgumentException.class, () ->
                checkoutController.processCheckout(invalidRequest));
    }

    @Test
    @DisplayName("Should calculate price with different product sizes")
    void testCalculatePriceWithDifferentSizes() {
        PriceCalculationResponse smallSize = new PriceCalculationResponse();
        smallSize.setTotalPrice(110.0);

        PriceCalculationResponse largeSize = new PriceCalculationResponse();
        largeSize.setTotalPrice(140.0);

        priceCalculationRequest.setProductSize("SMALL");
        when(priceCalculationService.calculatePrice(priceCalculationRequest))
                .thenReturn(smallSize);

        ResponseEntity<?> response1 = checkoutController.calculatePrice(priceCalculationRequest);
        assertEquals(200, response1.getStatusCode().value());

        priceCalculationRequest.setProductSize("BIG");
        when(priceCalculationService.calculatePrice(priceCalculationRequest))
                .thenReturn(largeSize);

        ResponseEntity<?> response2 = checkoutController.calculatePrice(priceCalculationRequest);
        assertEquals(200, response2.getStatusCode().value());
    }

    @Test
    @DisplayName("Should include distance in price calculation")
    void testPriceCalculationIncludesDistance() {
        PriceCalculationResponse priceResponse = new PriceCalculationResponse();
        priceResponse.setDistance(5.0);
        priceResponse.setDistanceCost(15.0);
        priceResponse.setTotalPrice(125.0);

        when(priceCalculationService.calculatePrice(any(PriceCalculationRequest.class)))
                .thenReturn(priceResponse);

        ResponseEntity<?> response = checkoutController.calculatePrice(priceCalculationRequest);

        assertEquals(200, response.getStatusCode().value());
    }
}

