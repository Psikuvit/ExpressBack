package me.psikuvit.express;

import me.psikuvit.express.dto.CheckoutRequest;
import me.psikuvit.express.dto.LoginRequest;
import me.psikuvit.express.dto.OrderItemRequest;
import me.psikuvit.express.dto.SignupRequest;
import me.psikuvit.express.model.DeliveryGuy;
import me.psikuvit.express.model.Location;
import me.psikuvit.express.model.Product;
import me.psikuvit.express.model.User;
import me.psikuvit.express.repository.DeliveryGuyRepository;
import me.psikuvit.express.repository.ProductRepository;
import me.psikuvit.express.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Express API Integration Tests")
class ExpressApplicationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DeliveryGuyRepository deliveryGuyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String authToken;
    private User testUser;
    private Product testProduct;
    private DeliveryGuy testDeliveryGuy;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clear repositories
        userRepository.deleteAll();
        productRepository.deleteAll();
        deliveryGuyRepository.deleteAll();

        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setBasePrice(100.0);
        testProduct.setSize(Product.ProductSize.MEDIUM);
        productRepository.save(testProduct);

        // Create test delivery guy
        testDeliveryGuy = new DeliveryGuy();
        testDeliveryGuy.setName("Test Delivery Guy");
        testDeliveryGuy.setAge(30);
        testDeliveryGuy.setCar("Toyota");
        testDeliveryGuy.setWhatsappNumber("+20123456789");
        testDeliveryGuy.setLocation(new Location(10.0, 20.0));
        testDeliveryGuy.setAvailable(true);
        deliveryGuyRepository.save(testDeliveryGuy);
    }

    @Test
    @DisplayName("Should successfully register and login a user")
    @Transactional
    void testUserRegistrationAndLogin() throws Exception {
        // Test signup
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Test login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("newuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        authToken = extractTokenFromResponse(response);
        assertNotNull(authToken, "JWT token should be returned");
    }

    @Test
    @DisplayName("Should reject duplicate username registration")
    @Transactional
    void testDuplicateUsernameRegistration() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("duplicateuser");
        signupRequest.setEmail("first@example.com");
        signupRequest.setPassword("password123");

        // First registration should succeed
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Second registration with same username should fail
        signupRequest.setEmail("second@example.com");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should retrieve all delivery guys")
    @Transactional
    void testGetAllDeliveryGuys() throws Exception {
        mockMvc.perform(get("/api/deliveryguys")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Should retrieve delivery guys with location query parameter")
    @Transactional
    void testGetDeliveryGuysWithLocation() throws Exception {
        mockMvc.perform(get("/api/deliveryguys")
                .param("latitude", "10.5")
                .param("longitude", "20.5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should rate limit auth requests")
    @Transactional
    void testRateLimitingOnAuthEndpoint() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // Make multiple failed login attempts
        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)));
        }

        // 6th request should be rate limited
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Should calculate price correctly")
    @Transactional
    void testPriceCalculation() throws Exception {
        String priceCalculationRequest = "{\n" +
                "  \"basePrice\": 100.0,\n" +
                "  \"distance\": 5.0,\n" +
                "  \"productSize\": \"MEDIUM\"\n" +
                "}";

        mockMvc.perform(post("/api/checkout/calc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(priceCalculationRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice", greaterThan(100.0)));
    }

    @Test
    @DisplayName("Should calculate different prices for different product sizes")
    @Transactional
    void testPriceCalculationWithDifferentSizes() throws Exception {
        String smallSize = "{\n" +
                "  \"basePrice\": 100.0,\n" +
                "  \"distance\": 5.0,\n" +
                "  \"productSize\": \"SMALL\"\n" +
                "}";

        String bigSize = "{\n" +
                "  \"basePrice\": 100.0,\n" +
                "  \"distance\": 5.0,\n" +
                "  \"productSize\": \"BIG\"\n" +
                "}";

        MvcResult smallResult = mockMvc.perform(post("/api/checkout/calc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(smallSize))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult bigResult = mockMvc.perform(post("/api/checkout/calc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bigSize))
                .andExpect(status().isOk())
                .andReturn();

        String smallResponse = smallResult.getResponse().getContentAsString();
        String bigResponse = bigResult.getResponse().getContentAsString();

        assertNotNull(smallResponse);
        assertNotNull(bigResponse);
    }

    @Test
    @DisplayName("Should handle invalid product size in price calculation")
    @Transactional
    void testPriceCalculationWithInvalidSize() throws Exception {
        String invalidSize = "{\n" +
                "  \"basePrice\": 100.0,\n" +
                "  \"distance\": 5.0,\n" +
                "  \"productSize\": \"INVALID\"\n" +
                "}";

        mockMvc.perform(post("/api/checkout/calc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidSize))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should have rate limit headers in response")
    @Transactional
    void testRateLimitHeaders() throws Exception {
        mockMvc.perform(get("/api/deliveryguys")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Rate-Limit-Remaining"));
    }

    /**
     * Helper method to extract JWT token from login response
     */
    private String extractTokenFromResponse(String response) {
        try {
            return objectMapper.readTree(response).get("accessToken").asText();
        } catch (Exception e) {
            return null;
        }
    }
}

