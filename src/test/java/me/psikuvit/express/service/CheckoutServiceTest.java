package me.psikuvit.express.service;

import me.psikuvit.express.dto.CheckoutRequest;
import me.psikuvit.express.dto.CheckoutResponse;
import me.psikuvit.express.dto.OrderItemRequest;
import me.psikuvit.express.model.*;
import me.psikuvit.express.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutService Tests")
class CheckoutServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryGuyRepository deliveryGuyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DistanceCalculationService distanceService;

    @Mock
    private WhatsAppService whatsAppService;

    @InjectMocks
    private CheckoutService checkoutService;

    private User testUser;
    private DeliveryGuy deliveryGuy;
    private Product product;
    private Location userLocation;
    private Location deliveryLocation;
    private CheckoutRequest checkoutRequest;

    @BeforeEach
    void setUp() {
        // Setup user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Setup delivery guy
        deliveryGuy = new DeliveryGuy();
        deliveryGuy.setId(1L);
        deliveryGuy.setName("Ahmed");
        deliveryGuy.setAge(30);
        deliveryGuy.setCar("Toyota");
        deliveryGuy.setWhatsappNumber("+20123456789");
        deliveryGuy.setLocation(new Location(10.5, 20.5));
        deliveryGuy.setAvailable(true);

        // Setup product
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setBasePrice(100.0);
        product.setSize(Product.ProductSize.MEDIUM);

        // Setup locations
        userLocation = new Location(10.0, 20.0);
        deliveryLocation = new Location(10.2, 20.2);

        // Setup checkout request
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setProducts(List.of(itemRequest));
        checkoutRequest.setDeliveryLocation(deliveryLocation);
    }

    @Test
    @DisplayName("Should successfully process checkout")
    void testProcessCheckoutSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(deliveryGuyRepository.findNearestDeliveryGuy(deliveryLocation.getLatitude(),
                deliveryLocation.getLongitude())).thenReturn(Optional.of(deliveryGuy));
        when(distanceService.calculateDistance(deliveryGuy.getLocation(), deliveryLocation))
                .thenReturn(2.5);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutResponse response = checkoutService.processCheckout(checkoutRequest, "testuser");

        assertNotNull(response);
        assertEquals("ASSIGNED", response.getStatus());
        verify(orderRepository).save(any(Order.class));
        verify(whatsAppService).sendOrderToDeliveryGuy(any(DeliveryGuy.class), any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testProcessCheckoutUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                checkoutService.processCheckout(checkoutRequest, "nonexistent"));
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testProcessCheckoutProductNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                checkoutService.processCheckout(checkoutRequest, "testuser"));
    }

    @Test
    @DisplayName("Should throw exception when no delivery guys available")
    void testProcessCheckoutNoDeliveryGuysAvailable() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(deliveryGuyRepository.findNearestDeliveryGuy(anyDouble(), anyDouble()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                checkoutService.processCheckout(checkoutRequest, "testuser"));
    }

    @Test
    @DisplayName("Should calculate correct total price with distance")
    void testCalculateTotalPriceWithDistance() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(deliveryGuyRepository.findNearestDeliveryGuy(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(deliveryGuy));
        when(distanceService.calculateDistance(any(), any())).thenReturn(5.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutResponse response = checkoutService.processCheckout(checkoutRequest, "testuser");

        assertNotNull(response);
        assertTrue(response.getTotalPrice() > 0);
    }

    @Test
    @DisplayName("Should validate products in checkout")
    void testValidateProductsInCheckout() {
        List<OrderItemRequest> products = new ArrayList<>();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);
        products.add(item);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Call the method that validates products
        List<CheckedOrderItem> validatedItems = checkoutService.validateProducts(products);

        assertNotNull(validatedItems);
        assertEquals(1, validatedItems.size());
    }

    @Test
    @DisplayName("Should create order with correct status")
    void testCreateOrderWithCorrectStatus() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(deliveryGuyRepository.findNearestDeliveryGuy(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(deliveryGuy));
        when(distanceService.calculateDistance(any(), any())).thenReturn(2.5);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutResponse response = checkoutService.processCheckout(checkoutRequest, "testuser");

        assertNotNull(response);
        assertEquals("ASSIGNED", response.getStatus());
    }
}

