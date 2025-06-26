package com.walletservice.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletservice.security.dto.JwtResponse;
import com.walletservice.security.dto.LoginRequest;
import com.walletservice.security.dto.SignupRequest;
import com.walletservice.security.model.User;
import com.walletservice.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class TestJwtUtil {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_EMAIL = "test@example.com";

    public String getTestToken() throws Exception {
        // Ensure test user exists
        createTestUserIfNotExists();

        // Login and get token
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        JwtResponse response = objectMapper.readValue(contentAsString, JwtResponse.class);

        return response.token();
    }

    private void createTestUserIfNotExists() throws Exception {
        if (!userRepository.existsByUsername(TEST_USERNAME)) {
            Set<String> roles = new HashSet<>();
            roles.add("USER");

            SignupRequest signupRequest = new SignupRequest(
                    TEST_USERNAME,
                    TEST_EMAIL,
                    TEST_PASSWORD,
                    roles
            );

            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isOk());
        }
    }
}
