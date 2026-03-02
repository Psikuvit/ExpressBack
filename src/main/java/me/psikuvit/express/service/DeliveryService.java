package me.psikuvit.express.service;

import me.psikuvit.express.dto.DeliveryOrderResponse;
import me.psikuvit.express.dto.DeliveryProfileResponse;
import me.psikuvit.express.dto.DeliveryRegistrationRequest;
import me.psikuvit.express.model.DeliveryGuy;
import me.psikuvit.express.model.Order;
import me.psikuvit.express.model.User;
import me.psikuvit.express.repository.DeliveryGuyRepository;
import me.psikuvit.express.repository.OrderRepository;
import me.psikuvit.express.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeliveryService {

    private final UserRepository userRepository;
    private final DeliveryGuyRepository deliveryGuyRepository;
    private final OrderRepository orderRepository;
    private final WhatsAppService whatsAppService;
    private final DistanceCalculationService distanceService;

    public DeliveryService(UserRepository userRepository,
                           DeliveryGuyRepository deliveryGuyRepository,
                           OrderRepository orderRepository,
                           WhatsAppService whatsAppService,
                           DistanceCalculationService distanceService) {
        this.userRepository = userRepository;
        this.deliveryGuyRepository = deliveryGuyRepository;
        this.orderRepository = orderRepository;
        this.whatsAppService = whatsAppService;
        this.distanceService = distanceService;
    }

    @Transactional
    public DeliveryProfileResponse register(String username, DeliveryRegistrationRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (deliveryGuyRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("You are already registered as a delivery guy");
        }

        DeliveryGuy deliveryGuy = new DeliveryGuy();
        deliveryGuy.setName(user.getUsername());
        deliveryGuy.setAge(request.getAge());
        deliveryGuy.setCar(request.getCar());
        deliveryGuy.setWhatsappNumber(request.getWhatsappNumber());
        deliveryGuy.setLocation(user.getLocation());
        deliveryGuy.setAvailable(true);
        deliveryGuy.setUser(user);

        deliveryGuyRepository.save(deliveryGuy);

        // Add ROLE_DELIVERY to user
        user.getRoles().add("ROLE_DELIVERY");
        userRepository.save(user);

        return toProfileResponse(deliveryGuy);
    }

    public DeliveryProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DeliveryGuy deliveryGuy = deliveryGuyRepository.findByUserId(user.getId())
                .orElse(null);

        if (deliveryGuy == null) {
            DeliveryProfileResponse response = new DeliveryProfileResponse(
                    null, user.getUsername(), null, null, null, null, null, false
            );
            return response;
        }

        return toProfileResponse(deliveryGuy);
    }

    public List<DeliveryOrderResponse> getAvailableOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> pendingOrders = orderRepository.findByStatusOrderByCreatedAtDesc(Order.OrderStatus.PENDING);

        List<DeliveryOrderResponse> responses = new ArrayList<>();
        for (Order order : pendingOrders) {
            List<DeliveryOrderResponse.OrderItemDetail> items = order.getItems().stream()
                    .map(item -> new DeliveryOrderResponse.OrderItemDetail(
                            item.getProduct().getName(),
                            item.getProduct().getSize().name(),
                            item.getQuantity(),
                            item.getPrice()
                    ))
                    .toList();

            // Calculate distance from delivery guy to delivery location
            double distance = 0;
            if (user.getLocation() != null && order.getDeliveryLocation() != null) {
                distance = distanceService.calculateDistance(user.getLocation(), order.getDeliveryLocation());
            }

            responses.add(new DeliveryOrderResponse(
                    order.getId(),
                    order.getUser().getUsername(),
                    items,
                    order.getTotalPrice(),
                    distance,
                    order.getDeliveryLocation(),
                    order.getStatus().name(),
                    order.getCreatedAt()
            ));
        }

        return responses;
    }

    @Transactional
    public DeliveryOrderResponse acceptOrder(String username, Long orderId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DeliveryGuy deliveryGuy = deliveryGuyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("You are not registered as a delivery guy"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("This order has already been taken");
        }

        // Assign delivery guy and update status
        order.setDeliveryGuy(deliveryGuy);
        order.setStatus(Order.OrderStatus.ASSIGNED);

        // Calculate distance
        double distance = 0;
        if (deliveryGuy.getLocation() != null && order.getDeliveryLocation() != null) {
            distance = distanceService.calculateDistance(deliveryGuy.getLocation(), order.getDeliveryLocation());
        }
        order.setDistance(distance);

        orderRepository.save(order);

        // Mark delivery guy as unavailable
        deliveryGuy.setAvailable(false);
        deliveryGuyRepository.save(deliveryGuy);

        // Send WhatsApp notification
        try {
            whatsAppService.sendMessage(
                    deliveryGuy.getWhatsappNumber(),
                    String.format("🚀 You accepted Order #%d!\n📍 Deliver to: %s\n💰 Total: $%.2f\n📏 Distance: %.1f km",
                            order.getId(),
                            order.getDeliveryLocation().getAddress(),
                            order.getTotalPrice(),
                            distance)
            );
        } catch (Exception e) {
            // Don't fail the order if WhatsApp fails
        }

        List<DeliveryOrderResponse.OrderItemDetail> items = order.getItems().stream()
                .map(item -> new DeliveryOrderResponse.OrderItemDetail(
                        item.getProduct().getName(),
                        item.getProduct().getSize().name(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        return new DeliveryOrderResponse(
                order.getId(),
                order.getUser().getUsername(),
                items,
                order.getTotalPrice(),
                distance,
                order.getDeliveryLocation(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }

    private DeliveryProfileResponse toProfileResponse(DeliveryGuy dg) {
        return new DeliveryProfileResponse(
                dg.getId(), dg.getName(), dg.getAge(), dg.getCar(),
                dg.getWhatsappNumber(), dg.getLocation(), dg.getAvailable(), true
        );
    }
}
