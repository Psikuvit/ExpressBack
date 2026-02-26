package me.psikuvit.express.service;

import me.psikuvit.express.dto.DeliveryGuyResponse;
import me.psikuvit.express.model.DeliveryGuy;
import me.psikuvit.express.model.Location;
import me.psikuvit.express.repository.DeliveryGuyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryGuyService Tests")
class DeliveryGuyServiceTest {

    @Mock
    private DeliveryGuyRepository deliveryGuyRepository;

    @Mock
    private DistanceCalculationService distanceService;

    @InjectMocks
    private DeliveryGuyService deliveryGuyService;

    private DeliveryGuy deliveryGuy1;
    private DeliveryGuy deliveryGuy2;
    private Location userLocation;

    @BeforeEach
    void setUp() {
        userLocation = new Location(10.0, 20.0);

        deliveryGuy1 = new DeliveryGuy();
        deliveryGuy1.setId(1L);
        deliveryGuy1.setName("Ahmed");
        deliveryGuy1.setAge(30);
        deliveryGuy1.setCar("Toyota");
        deliveryGuy1.setWhatsappNumber("+20123456789");
        deliveryGuy1.setLocation(new Location(10.5, 20.5));
        deliveryGuy1.setAvailable(true);

        deliveryGuy2 = new DeliveryGuy();
        deliveryGuy2.setId(2L);
        deliveryGuy2.setName("Mohamed");
        deliveryGuy2.setAge(28);
        deliveryGuy2.setCar("Honda");
        deliveryGuy2.setWhatsappNumber("+20987654321");
        deliveryGuy2.setLocation(new Location(11.0, 21.0));
        deliveryGuy2.setAvailable(true);
    }

    @Test
    @DisplayName("Should retrieve all delivery guys without location")
    void testGetAllDeliveryGuysWithoutLocation() {
        when(deliveryGuyRepository.findAll()).thenReturn(Arrays.asList(deliveryGuy1, deliveryGuy2));

        List<DeliveryGuyResponse> response = deliveryGuyService.getAllDeliveryGuys(null);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Ahmed", response.get(0).getName());
        assertEquals("Mohamed", response.get(1).getName());
        verify(deliveryGuyRepository).findAll();
    }

    @Test
    @DisplayName("Should retrieve all delivery guys with location and calculate distances")
    void testGetAllDeliveryGuysWithLocation() {
        when(deliveryGuyRepository.findAll()).thenReturn(Arrays.asList(deliveryGuy1, deliveryGuy2));
        when(distanceService.calculateDistance(deliveryGuy1.getLocation(), userLocation)).thenReturn(0.5);
        when(distanceService.calculateDistance(deliveryGuy2.getLocation(), userLocation)).thenReturn(1.5);

        List<DeliveryGuyResponse> response = deliveryGuyService.getAllDeliveryGuys(userLocation);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(0.5, response.get(0).getDistanceFromUser(), 0.001);
        assertEquals(1.5, response.get(1).getDistanceFromUser(), 0.001);
    }

    @Test
    @DisplayName("Should sort delivery guys by distance from user")
    void testDeliveryGuysSortedByDistance() {
        when(deliveryGuyRepository.findAll()).thenReturn(Arrays.asList(deliveryGuy2, deliveryGuy1)); // Unsorted
        when(distanceService.calculateDistance(deliveryGuy1.getLocation(), userLocation)).thenReturn(0.5);
        when(distanceService.calculateDistance(deliveryGuy2.getLocation(), userLocation)).thenReturn(1.5);

        List<DeliveryGuyResponse> response = deliveryGuyService.getAllDeliveryGuys(userLocation);

        assertEquals("Ahmed", response.get(0).getName(), "Closest delivery guy should be first");
        assertEquals("Mohamed", response.get(1).getName(), "Farthest delivery guy should be last");
    }

    @Test
    @DisplayName("Should include delivery guy details in response")
    void testDeliveryGuyResponseDetails() {
        when(deliveryGuyRepository.findAll()).thenReturn(Arrays.asList(deliveryGuy1));
        when(distanceService.calculateDistance(deliveryGuy1.getLocation(), userLocation)).thenReturn(0.5);

        List<DeliveryGuyResponse> response = deliveryGuyService.getAllDeliveryGuys(userLocation);

        DeliveryGuyResponse guyResponse = response.get(0);
        assertEquals(1L, guyResponse.getId());
        assertEquals("Ahmed", guyResponse.getName());
        assertEquals(30, guyResponse.getAge());
        assertEquals("Toyota", guyResponse.getCar());
        assertEquals("+20123456789", guyResponse.getWhatsappNumber());
        assertTrue(guyResponse.getAvailable());
    }

    @Test
    @DisplayName("Should return empty list when no delivery guys available")
    void testGetAllDeliveryGuysEmpty() {
        when(deliveryGuyRepository.findAll()).thenReturn(Arrays.asList());

        List<DeliveryGuyResponse> response = deliveryGuyService.getAllDeliveryGuys(userLocation);

        assertNotNull(response);
        assertEquals(0, response.size());
    }
}

