package me.psikuvit.express.service;

import me.psikuvit.express.dto.*;
import me.psikuvit.express.model.*;
import me.psikuvit.express.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService {

    private final ProductRepository productRepository;

    private final OrderRepository orderRepository;

    private final DeliveryGuyRepository deliveryGuyRepository;

    private final UserRepository userRepository;

    private final DistanceCalculationService distanceService;

    private final WhatsAppService whatsAppService;

    public CheckoutService(ProductRepository productRepository, OrderRepository orderRepository, DeliveryGuyRepository deliveryGuyRepository, UserRepository userRepository, DistanceCalculationService distanceService, WhatsAppService whatsAppService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.deliveryGuyRepository = deliveryGuyRepository;
        this.userRepository = userRepository;
        this.distanceService = distanceService;
        this.whatsAppService = whatsAppService;
    }

    @Transactional
    public CheckoutResponse processCheckout(CheckoutRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate and check products
        List<CheckedOrderItem> checkedItems = validateProducts(request.getProducts());

        // Calculate estimated distance from user location
        double distance = 0;

        // Calculate total price
        double totalPrice = calculateTotalPrice(checkedItems, distance);

        // Create order as PENDING — delivery guys will pick it up
        Order order = new Order();
        order.setUser(user);
        order.setDeliveryLocation(request.getDeliveryLocation());
        order.setDistance(distance);
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.OrderStatus.PENDING);

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemReq : request.getProducts()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(product.getBasePrice() * itemReq.getQuantity());
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        // Save order
        order = orderRepository.save(order);

        return new CheckoutResponse(
                order.getId(),
                "Order placed! A delivery driver will accept it shortly.",
                checkedItems,
                totalPrice,
                distance,
                null
        );
    }

    private List<CheckedOrderItem> validateProducts(List<OrderItemRequest> products) {
        List<CheckedOrderItem> checkedItems = new ArrayList<>();

        for (OrderItemRequest itemReq : products) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product with ID " + itemReq.getProductId() + " not found"));

            if (!product.getAvailable()) {
                throw new RuntimeException("Product " + product.getName() + " is not available");
            }

            CheckedOrderItem checkedItem = new CheckedOrderItem(
                    product.getId(),
                    product.getName(),
                    product.getSize(),
                    itemReq.getQuantity(),
                    product.getBasePrice(),
                    true
            );
            checkedItems.add(checkedItem);
        }

        return checkedItems;
    }

    private DeliveryGuy findNearestDeliveryGuy(Location userLocation) {
        List<DeliveryGuy> availableGuys = deliveryGuyRepository.findByAvailable(true);

        if (availableGuys.isEmpty()) {
            return null;
        }

        DeliveryGuy nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (DeliveryGuy guy : availableGuys) {
            double distance = distanceService.calculateDistance(guy.getLocation(), userLocation);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = guy;
            }
        }

        return nearest;
    }

    private double calculateTotalPrice(List<CheckedOrderItem> items, double distance) {
        double basePrice = 0;
        double sizeFee = 0;

        for (CheckedOrderItem item : items) {
            double itemTotal = item.getBasePrice() * item.getQuantity();
            basePrice += itemTotal;

            // Add size-based fee
            sizeFee += getSizeFee(item.getSize()) * item.getQuantity();
        }

        double distanceFee = distance * 0.5; // $0.5 per km

        return basePrice + sizeFee + distanceFee;
    }

    private double getSizeFee(Product.ProductSize size) {
        return switch (size) {
            case SMALL -> 1.0;
            case MEDIUM -> 2.5;
            case BIG -> 5.0;
        };
    }

    public PriceCalculationResponse calculatePrice(List<OrderItemRequest> products, Location userLocation) {
        List<CheckedOrderItem> checkedItems = validateProducts(products);

        DeliveryGuy nearestDeliveryGuy = findNearestDeliveryGuy(userLocation);
        if (nearestDeliveryGuy == null) {
            throw new RuntimeException("No available delivery guys at the moment");
        }

        double distance = distanceService.calculateDistance(nearestDeliveryGuy.getLocation(), userLocation);

        double basePrice = 0;
        double sizeFee = 0;

        for (CheckedOrderItem item : checkedItems) {
            double itemTotal = item.getBasePrice() * item.getQuantity();
            basePrice += itemTotal;
            sizeFee += getSizeFee(item.getSize()) * item.getQuantity();
        }

        double distanceFee = distance * 0.5;
        double totalPrice = basePrice + sizeFee + distanceFee;

        String breakdown = String.format(
                "Base Price: $%.2f | Size Fee: $%.2f | Distance Fee (%.2f km): $%.2f",
                basePrice, sizeFee, distance, distanceFee
        );

        return new PriceCalculationResponse(basePrice, sizeFee, distanceFee, totalPrice, distance, breakdown);
    }
}

