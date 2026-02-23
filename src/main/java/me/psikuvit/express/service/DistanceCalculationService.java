package me.psikuvit.express.service;

import me.psikuvit.express.model.Location;
import org.springframework.stereotype.Service;

@Service
public class DistanceCalculationService {

    /**
     * Calculate distance between two locations using Haversine formula
     * Returns distance in kilometers
     */
    public double calculateDistance(Location from, Location to) {
        if (from == null || to == null ||
            from.getLatitude() == null || from.getLongitude() == null ||
            to.getLatitude() == null || to.getLongitude() == null) {
            return 0.0;
        }

        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double latDistance = Math.toRadians(to.getLatitude() - from.getLatitude());
        double lonDistance = Math.toRadians(to.getLongitude() - from.getLongitude());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(from.getLatitude())) * Math.cos(Math.toRadians(to.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}

