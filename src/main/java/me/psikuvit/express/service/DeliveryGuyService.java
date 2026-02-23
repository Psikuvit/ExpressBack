package me.psikuvit.express.service;

import me.psikuvit.express.dto.DeliveryGuyResponse;
import me.psikuvit.express.model.DeliveryGuy;
import me.psikuvit.express.model.Location;
import me.psikuvit.express.repository.DeliveryGuyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DeliveryGuyService {

    private final DeliveryGuyRepository deliveryGuyRepository;

    private final DistanceCalculationService distanceService;

    public DeliveryGuyService(DeliveryGuyRepository deliveryGuyRepository, DistanceCalculationService distanceService) {
        this.deliveryGuyRepository = deliveryGuyRepository;
        this.distanceService = distanceService;
    }

    public List<DeliveryGuyResponse> getAllDeliveryGuys(Location userLocation) {
        List<DeliveryGuy> deliveryGuys = deliveryGuyRepository.findAll();
        List<DeliveryGuyResponse> responses = new ArrayList<>();

        for (DeliveryGuy guy : deliveryGuys) {
            double distance = 0.0;
            if (userLocation != null) {
                distance = distanceService.calculateDistance(guy.getLocation(), userLocation);
            }

            DeliveryGuyResponse response = new DeliveryGuyResponse(
                    guy.getId(),
                    guy.getName(),
                    guy.getAge(),
                    guy.getCar(),
                    guy.getLocation(),
                    guy.getAvailable(),
                    distance
            );
            responses.add(response);
        }

        // Sort by distance if user location is provided
        if (userLocation != null) {
            responses.sort(Comparator.comparingDouble(DeliveryGuyResponse::getDistanceFromUser));
        }

        return responses;
    }
}

