package com.syfproject.img_store.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    public void testRegisterUserValid() throws Exception {
        String userJson = "{\"username\":\"testuser\",\"password\":\"Test123423\",\"email\":\"testuser@example.com\"}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User registered successfully")));
    }

    @Test
    @Order(2)
    public void testRegisterDuplicateUser() throws Exception {
        String userJson = "{\"username\":\"duplicateUser\",\"password\":\"Test123423\",\"email\":\"dup@example.com\"}";
        // First registration should succeed.
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());
        // Second registration should fail with 400 Bad Request.
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username is already taken")));
    }

    // Validation tests for user registration

    // Username has less than 5 characters.
    @Test
    @Order(3)
    public void testRegisterUserInvalidUsername() throws Exception {
        String userJson = "{\"username\":\"abc\",\"password\":\"Test1234\",\"email\":\"valid@example.com\"}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username must have at least 5 characters")));
    }

    // Password has less than 6 characters.
    @Test
    @Order(4)
    public void testRegisterUserPasswordTooShort() throws Exception {
        String userJson = "{\"username\":\"validUser\",\"password\":\"T1a\",\"email\":\"valid@example.com\"}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Password must be at least 6 characters")));
    }

    // Password missing uppercase letter.
    @Test
    @Order(5)
    public void testRegisterUserPasswordNoUppercase() throws Exception {
        String userJson = "{\"username\":\"validUser\",\"password\":\"password1\",\"email\":\"valid@example.com\"}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Password must contain at least one uppercase letter")));
    }

    // Password missing digit.
    @Test
    @Order(6)
    public void testRegisterUserPasswordNoDigit() throws Exception {
        String userJson = "{\"username\":\"validUser\",\"password\":\"Password\",\"email\":\"valid@example.com\"}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Password must contain at least one uppercase letter and one number")));
    }

    // Invalid email address.
    @Test
    @Order(7)
    public void testRegisterUserInvalidEmail() throws Exception {
        String userJson = "{\"username\":\"validUser\",\"password\":\"Password1\",\"email\":\"invalid-email\"}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email address")));
    }

    @Test
    @Order(8)
    public void testLoginUser() throws Exception {
        // Register user
        String userJson = "{\"username\":\"loginUser\",\"password\":\"Test1234\",\"email\":\"login@example.com\"}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());

        // Login with correct credentials
        String loginJson = "{\"username\":\"loginUser\",\"password\":\"Test1234\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, String> loginResponse = objectMapper.readValue(responseBody, Map.class);
        System.out.println("JWT Token: " + loginResponse.get("token"));
    }

    @Test
    @Order(9)
    public void testLoginWithWrongCredentials() throws Exception {
        String loginJson = "{\"username\":\"nonexistent\",\"password\":\"wrongpass\"}";
        mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid username or password")));
    }
}
