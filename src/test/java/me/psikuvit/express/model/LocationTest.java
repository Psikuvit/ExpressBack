package me.psikuvit.express.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Location Model Tests")
class LocationTest {

    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location();
    }

    @Test
    @DisplayName("Should create location with latitude and longitude")
    void testLocationCreation() {
        Location loc = new Location(30.0444, 31.2357);

        assertEquals(30.0444, loc.getLatitude());
        assertEquals(31.2357, loc.getLongitude());
    }

    @Test
    @DisplayName("Should set and get latitude")
    void testSetGetLatitude() {
        location.setLatitude(10.5);

        assertEquals(10.5, location.getLatitude());
    }

    @Test
    @DisplayName("Should set and get longitude")
    void testSetGetLongitude() {
        location.setLongitude(20.5);

        assertEquals(20.5, location.getLongitude());
    }

    @Test
    @DisplayName("Should handle negative coordinates")
    void testNegativeCoordinates() {
        Location loc = new Location(-33.8688, 151.2093);

        assertEquals(-33.8688, loc.getLatitude());
        assertEquals(151.2093, loc.getLongitude());
    }

    @Test
    @DisplayName("Should handle zero coordinates")
    void testZeroCoordinates() {
        Location loc = new Location(0.0, 0.0);

        assertEquals(0.0, loc.getLatitude());
        assertEquals(0.0, loc.getLongitude());
    }

    @Test
    @DisplayName("Should be equal when coordinates match")
    void testLocationEquality() {
        Location loc1 = new Location(10.5, 20.5);
        Location loc2 = new Location(10.5, 20.5);

        assertEquals(loc1, loc2);
    }

    @Test
    @DisplayName("Should not be equal when coordinates differ")
    void testLocationInequality() {
        Location loc1 = new Location(10.5, 20.5);
        Location loc2 = new Location(11.5, 21.5);

        assertNotEquals(loc1, loc2);
    }

    @Test
    @DisplayName("Should update coordinates")
    void testUpdateCoordinates() {
        location.setLatitude(10.0);
        location.setLongitude(20.0);

        location.setLatitude(15.0);
        location.setLongitude(25.0);

        assertEquals(15.0, location.getLatitude());
        assertEquals(25.0, location.getLongitude());
    }

    @Test
    @DisplayName("Should have consistent hash code for equal locations")
    void testLocationHashCode() {
        Location loc1 = new Location(10.5, 20.5);
        Location loc2 = new Location(10.5, 20.5);

        assertEquals(loc1.hashCode(), loc2.hashCode());
    }
}

