package me.psikuvit.express.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Model Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("Should create user with all fields")
    void testUserCreation() {
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword", user.getPassword());
    }

    @Test
    @DisplayName("Should initialize empty roles set")
    void testRolesInitialization() {
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should add roles to user")
    void testAddRoles() {
        user.getRoles().add("ROLE_USER");
        user.getRoles().add("ROLE_ADMIN");

        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains("ROLE_USER"));
        assertTrue(user.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should set location to user")
    void testSetLocation() {
        Location location = new Location(10.0, 20.0);
        user.setLocation(location);

        assertNotNull(user.getLocation());
        assertEquals(10.0, user.getLocation().getLatitude());
        assertEquals(20.0, user.getLocation().getLongitude());
    }

    @Test
    @DisplayName("Should update user information")
    void testUpdateUserInformation() {
        user.setUsername("originaluser");
        user.setEmail("original@example.com");

        user.setUsername("updateduser");
        user.setEmail("updated@example.com");

        assertEquals("updateduser", user.getUsername());
        assertEquals("updated@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Should handle null location")
    void testNullLocation() {
        user.setLocation(null);
        assertNull(user.getLocation());
    }

    @Test
    @DisplayName("Should properly initialize with constructor")
    void testConstructorInitialization() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        Location location = new Location(10.0, 20.0);

        User newUser = new User(1L, "testuser", "test@example.com", "password", roles, location);

        assertEquals(1L, newUser.getId());
        assertEquals("testuser", newUser.getUsername());
        assertEquals("test@example.com", newUser.getEmail());
        assertEquals("password", newUser.getPassword());
        assertEquals(1, newUser.getRoles().size());
        assertNotNull(newUser.getLocation());
    }
}

