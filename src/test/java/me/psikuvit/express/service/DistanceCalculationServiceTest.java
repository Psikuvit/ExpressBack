package me.psikuvit.express.service;

import me.psikuvit.express.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DistanceCalculationService Tests")
class DistanceCalculationServiceTest {

    private DistanceCalculationService distanceCalculationService;

    @BeforeEach
    void setUp() {
        distanceCalculationService = new DistanceCalculationService();
    }

    @Test
    @DisplayName("Should calculate distance between two locations using Haversine formula")
    void testCalculateDistanceHaversine() {
        Location location1 = new Location(30.0444, 31.2357); // Cairo, Egypt
        Location location2 = new Location(30.0626, 31.2471); // Cairo, Egypt (nearby)

        double distance = distanceCalculationService.calculateDistance(location1, location2);

        assertNotNull(distance);
        assertTrue(distance > 0, "Distance should be positive");
        assertTrue(distance < 10, "Distance between nearby locations should be less than 10 km");
    }

    @Test
    @DisplayName("Should return zero distance for same location")
    void testZeroDistanceForSameLocation() {
        Location location = new Location(30.0444, 31.2357);

        double distance = distanceCalculationService.calculateDistance(location, location);

        assertEquals(0.0, distance, 0.01, "Distance between same locations should be zero");
    }

    @Test
    @DisplayName("Should handle different coordinate systems correctly")
    void testDistanceCalculationWithDifferentCoordinates() {
        Location city1 = new Location(40.7128, -74.0060); // New York
        Location city2 = new Location(34.0522, -118.2437); // Los Angeles

        double distance = distanceCalculationService.calculateDistance(city1, city2);

        assertNotNull(distance);
        assertTrue(distance > 0);
        assertTrue(distance > 3000, "Distance between NYC and LA should be more than 3000 km");
        assertTrue(distance < 4000, "Distance between NYC and LA should be less than 4000 km");
    }

    @Test
    @DisplayName("Should calculate symmetric distance (A->B equals B->A)")
    void testSymmetricDistance() {
        Location location1 = new Location(30.0444, 31.2357);
        Location location2 = new Location(30.1234, 31.3456);

        double distance1to2 = distanceCalculationService.calculateDistance(location1, location2);
        double distance2to1 = distanceCalculationService.calculateDistance(location2, location1);

        assertEquals(distance1to2, distance2to1, 0.001, "Distance should be symmetric");
    }

    @Test
    @DisplayName("Should handle negative coordinates (Southern and Western hemispheres)")
    void testNegativeCoordinates() {
        Location sydney = new Location(-33.8688, 151.2093); // Sydney, Australia
        Location buenos_aires = new Location(-34.6037, -58.3816); // Buenos Aires, Argentina

        double distance = distanceCalculationService.calculateDistance(sydney, buenos_aires);

        assertNotNull(distance);
        assertTrue(distance > 0);
        assertTrue(distance > 10000, "Distance between Sydney and Buenos Aires should be more than 10000 km");
    }

    @Test
    @DisplayName("Should handle locations at poles")
    void testLocationsNearPoles() {
        Location northPole = new Location(89.9, 0);
        Location arctic = new Location(70.0, 0);

        double distance = distanceCalculationService.calculateDistance(northPole, arctic);

        assertNotNull(distance);
        assertTrue(distance > 0);
        assertTrue(distance < 2500, "Distance near poles should be reasonable");
    }

    @Test
    @DisplayName("Should handle equatorial locations")
    void testEquatorialLocations() {
        Location equator1 = new Location(0.0, 0.0);
        Location equator2 = new Location(0.0, 1.0);

        double distance = distanceCalculationService.calculateDistance(equator1, equator2);

        assertNotNull(distance);
        assertTrue(distance > 0);
        assertTrue(distance > 100, "Distance along equator should be more than 100 km");
        assertTrue(distance < 120, "Distance along equator should be less than 120 km");
    }
}

