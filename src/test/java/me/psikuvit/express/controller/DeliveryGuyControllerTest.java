package me.psikuvit.express.controller;

import me.psikuvit.express.dto.DeliveryGuyResponse;
import me.psikuvit.express.model.Location;
import me.psikuvit.express.service.DeliveryGuyService;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryGuyController Tests")
class DeliveryGuyControllerTest {

    @Mock
    private DeliveryGuyService deliveryGuyService;

    @InjectMocks
    private DeliveryGuyController deliveryGuyController;

    private List<DeliveryGuyResponse> deliveryGuyResponses;
    private Location userLocation;

    @BeforeEach
    void setUp() {
        userLocation = new Location(10.0, 20.0);

        DeliveryGuyResponse guy1 = new DeliveryGuyResponse(
                1L, "Ahmed", 30, "Toyota", "+20123456789",
                new Location(10.5, 20.5), true, 0.5
        );

        DeliveryGuyResponse guy2 = new DeliveryGuyResponse(
                2L, "Mohamed", 28, "Honda", "+20987654321",
                new Location(11.0, 21.0), true, 1.5
        );

        deliveryGuyResponses = Arrays.asList(guy1, guy2);
    }

    @Test
    @DisplayName("Should retrieve all delivery guys")
    @WithMockUser(username = "testuser")
    void testGetAllDeliveryGuys() {
        when(deliveryGuyService.getAllDeliveryGuys(any(Location.class)))
                .thenReturn(deliveryGuyResponses);

        ResponseEntity<?> response = deliveryGuyController.getAllDeliveryGuys(userLocation);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(deliveryGuyService).getAllDeliveryGuys(any(Location.class));
    }

    @Test
    @DisplayName("Should return delivery guys sorted by distance")
    @WithMockUser(username = "testuser")
    void testGetDeliveryGuysSortedByDistance() {
        when(deliveryGuyService.getAllDeliveryGuys(userLocation))
                .thenReturn(deliveryGuyResponses);

        ResponseEntity<?> response = deliveryGuyController.getAllDeliveryGuys(userLocation);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        List<DeliveryGuyResponse> guys = (List<DeliveryGuyResponse>) response.getBody();
        assertNotNull(guys);
        assertEquals(2, guys.size());
        assertEquals(0.5, guys.get(0).getDistanceFromUser(), 0.001);
        assertEquals(1.5, guys.get(1).getDistanceFromUser(), 0.001);
    }

    @Test
    @DisplayName("Should include delivery guy details in response")
    @WithMockUser(username = "testuser")
    void testDeliveryGuyDetailsInResponse() {
        when(deliveryGuyService.getAllDeliveryGuys(userLocation))
                .thenReturn(deliveryGuyResponses);

        ResponseEntity<?> response = deliveryGuyController.getAllDeliveryGuys(userLocation);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        List<DeliveryGuyResponse> guys = (List<DeliveryGuyResponse>) response.getBody();
        assertNotNull(guys);

        DeliveryGuyResponse firstGuy = guys.get(0);
        assertEquals(1L, firstGuy.getId());
        assertEquals("Ahmed", firstGuy.getName());
        assertEquals(30, firstGuy.getAge());
        assertEquals("Toyota", firstGuy.getCar());
        assertEquals("+20123456789", firstGuy.getWhatsappNumber());
        assertTrue(firstGuy.getAvailable());
    }

    @Test
    @DisplayName("Should handle empty list of delivery guys")
    @WithMockUser(username = "testuser")
    void testEmptyDeliveryGuyList() {
        when(deliveryGuyService.getAllDeliveryGuys(any(Location.class)))
                .thenReturn(new ArrayList<>());

        ResponseEntity<?> response = deliveryGuyController.getAllDeliveryGuys(userLocation);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        List<DeliveryGuyResponse> guys = (List<DeliveryGuyResponse>) response.getBody();
        assertNotNull(guys);
        assertEquals(0, guys.size());
    }

    @Test
    @DisplayName("Should handle null user location")
    @WithMockUser(username = "testuser")
    void testNullUserLocation() {
        when(deliveryGuyService.getAllDeliveryGuys(null))
                .thenReturn(deliveryGuyResponses);

        ResponseEntity<?> response = deliveryGuyController.getAllDeliveryGuys(null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should include WhatsApp number for delivery guys")
    @WithMockUser(username = "testuser")
    void testWhatsAppNumberIncluded() {
        when(deliveryGuyService.getAllDeliveryGuys(userLocation))
                .thenReturn(deliveryGuyResponses);

        ResponseEntity<?> response = deliveryGuyController.getAllDeliveryGuys(userLocation);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        List<DeliveryGuyResponse> guys = (List<DeliveryGuyResponse>) response.getBody();
        assertNotNull(guys);
        assertTrue(guys.stream().allMatch(g -> g.getWhatsappNumber() != null));
        assertTrue(guys.stream().allMatch(g -> g.getWhatsappNumber().startsWith("+")));
    }
}

