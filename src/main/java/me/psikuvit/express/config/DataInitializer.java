package me.psikuvit.express.config;

import me.psikuvit.express.model.DeliveryGuy;
import me.psikuvit.express.model.Location;
import me.psikuvit.express.model.Product;
import me.psikuvit.express.model.User;
import me.psikuvit.express.repository.DeliveryGuyRepository;
import me.psikuvit.express.repository.ProductRepository;
import me.psikuvit.express.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DeliveryGuyRepository deliveryGuyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        // Initialize sample products if none exist
        if (productRepository.count() == 0) {
            Product p1 = new Product();
            p1.setName("Pizza Margherita");
            p1.setDescription("Classic Italian pizza with tomato and mozzarella");
            p1.setSize(Product.ProductSize.MEDIUM);
            p1.setBasePrice(12.99);
            p1.setAvailable(true);

            Product p2 = new Product();
            p2.setName("Burger Deluxe");
            p2.setDescription("Premium beef burger with special sauce");
            p2.setSize(Product.ProductSize.SMALL);
            p2.setBasePrice(8.99);
            p2.setAvailable(true);

            Product p3 = new Product();
            p3.setName("Family Meal Box");
            p3.setDescription("Large meal box for the whole family");
            p3.setSize(Product.ProductSize.BIG);
            p3.setBasePrice(29.99);
            p3.setAvailable(true);

            Product p4 = new Product();
            p4.setName("Salad Bowl");
            p4.setDescription("Fresh garden salad");
            p4.setSize(Product.ProductSize.SMALL);
            p4.setBasePrice(6.99);
            p4.setAvailable(true);

            Product p5 = new Product();
            p5.setName("Pasta Carbonara");
            p5.setDescription("Creamy Italian pasta");
            p5.setSize(Product.ProductSize.MEDIUM);
            p5.setBasePrice(11.99);
            p5.setAvailable(true);

            productRepository.save(p1);
            productRepository.save(p2);
            productRepository.save(p3);
            productRepository.save(p4);
            productRepository.save(p5);

            System.out.println("Sample products created!");
        }

        // Initialize sample delivery guys if none exist
        if (deliveryGuyRepository.count() == 0) {
            DeliveryGuy dg1 = new DeliveryGuy();
            dg1.setName("John Smith");
            dg1.setAge(28);
            dg1.setCar("Honda Civic");
            dg1.setWhatsappNumber("+14155551001");
            dg1.setLocation(new Location(40.7128, -74.0060, "New York, NY"));
            dg1.setAvailable(true);

            DeliveryGuy dg2 = new DeliveryGuy();
            dg2.setName("Maria Garcia");
            dg2.setAge(32);
            dg2.setCar("Toyota Corolla");
            dg2.setWhatsappNumber("+14155551002");
            dg2.setLocation(new Location(40.7589, -73.9851, "Times Square, NY"));
            dg2.setAvailable(true);

            DeliveryGuy dg3 = new DeliveryGuy();
            dg3.setName("Ahmed Hassan");
            dg3.setAge(25);
            dg3.setCar("Ford Focus");
            dg3.setWhatsappNumber("+14155551003");
            dg3.setLocation(new Location(40.7614, -73.9776, "Central Park, NY"));
            dg3.setAvailable(true);

            DeliveryGuy dg4 = new DeliveryGuy();
            dg4.setName("Lisa Chen");
            dg4.setAge(30);
            dg4.setCar("Mazda 3");
            dg4.setWhatsappNumber("+14155551004");
            dg4.setLocation(new Location(40.7480, -73.9862, "Empire State Building, NY"));
            dg4.setAvailable(true);

            deliveryGuyRepository.save(dg1);
            deliveryGuyRepository.save(dg2);
            deliveryGuyRepository.save(dg3);
            deliveryGuyRepository.save(dg4);

            System.out.println("Sample delivery guys created!");
        }

        // Create a test user if none exist
        if (userRepository.count() == 0) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setPassword(passwordEncoder.encode("password123"));
            Set<String> roles = new HashSet<>();
            roles.add("ROLE_USER");
            testUser.setRoles(roles);
            testUser.setLocation(new Location(40.7128, -74.0060, "New York, NY"));

            userRepository.save(testUser);
            System.out.println("Test user created! Username: testuser, Password: password123");
        }
    }
}

