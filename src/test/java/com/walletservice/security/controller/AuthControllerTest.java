package com.walletservice.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletservice.security.dto.LoginRequest;
import com.walletservice.security.dto.SignupRequest;
import com.walletservice.security.model.User;
import com.walletservice.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.walletservice.config.TestSecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testSignup() throws Exception {
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        SignupRequest signupRequest = new SignupRequest(
                "testuser",
                "test@example.com",
                "password123",
                roles
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));
    }

    @Test
    void testSignupWithDuplicateUsername() throws Exception {
        // Create a user first
        User user = new User("testuser", passwordEncoder.encode("password123"), "test@example.com");
        userRepository.save(user);

        // Try to register with the same username
        Set<String> roles = new HashSet<>();
        roles.add("USER");

        SignupRequest signupRequest = new SignupRequest(
                "testuser",
                "another@example.com",
                "password123",
                roles
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    void testSignin() throws Exception {
        // Create a user first
        User user = new User("testuser", passwordEncoder.encode("password123"), "test@example.com");
        user.addRole("USER");
        userRepository.save(user);

        // Login with the user
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testSigninWithWrongPassword() throws Exception {
        // Create a user first
        User user = new User("testuser", passwordEncoder.encode("password123"), "test@example.com");
        userRepository.save(user);

        // Login with wrong password
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
