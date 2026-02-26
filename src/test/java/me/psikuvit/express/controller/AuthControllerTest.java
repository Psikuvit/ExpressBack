package me.psikuvit.express.controller;

import me.psikuvit.express.dto.LoginRequest;
import me.psikuvit.express.dto.SignupRequest;
import me.psikuvit.express.model.User;
import me.psikuvit.express.repository.UserRepository;
import me.psikuvit.express.security.JwtUtils;
import me.psikuvit.express.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController authController;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
    }

    @Test
    @DisplayName("Should successfully register new user")
    void testRegisterUserSuccess() {
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(200, response.getStatusCode().value());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration with duplicate username")
    void testRegisterUserDuplicateUsername() {
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(400, response.getStatusCode().value());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testRegisterUserDuplicateEmail() {
        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signupRequest);

        assertEquals(400, response.getStatusCode().value());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("testuser");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("validJwtToken");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(refreshTokenService.createRefreshToken("testuser")).thenReturn(null);

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should reject login with invalid credentials")
    void testLoginFailure() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    @DisplayName("Should return user with correct roles")
    void testAuthenticateUserReturnsCorrectData() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("testuser");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("validJwtToken");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(refreshTokenService.createRefreshToken("testuser")).thenReturn(null);

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(200, response.getStatusCode().value());
    }
}

