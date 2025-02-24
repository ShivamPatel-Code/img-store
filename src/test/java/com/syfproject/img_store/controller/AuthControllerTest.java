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

/**
 * Integration tests for AuthController endpoints.
 * Tests user registration and login including validations.
 */
@SpringBootTest(properties = {"kafka.enabled=false"})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
        "jwt.secret=abcdefghijklmnopqrstuvwxyz012345", // at least 32 characters for HS256
        "jwt.expiration=3600000"
})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test valid user registration. Expects success message.
     * New user data now includes firstname and lastname.
     */
    @Test
    @Order(1)
    public void testRegisterUserValid() throws Exception {
        String userJson = "{" +
                "\"username\":\"testuser\"," +
                "\"password\":\"Test123423\"," +
                "\"email\":\"testuser@example.com\"," +
                "\"firstname\":\"Test\"," +
                "\"lastname\":\"User\"" +
                "}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User registered successfully")));
    }

    /**
     * Test duplicate user registration. Second attempt should fail.
     */
    @Test
    @Order(2)
    public void testRegisterDuplicateUser() throws Exception {
        String userJson = "{" +
                "\"username\":\"duplicateUser\"," +
                "\"password\":\"Test123423\"," +
                "\"email\":\"dup@example.com\"," +
                "\"firstname\":\"Dup\"," +
                "\"lastname\":\"User\"" +
                "}";
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

    /**
     * Test registration fails if username is too short.
     */
    @Test
    @Order(3)
    public void testRegisterUserInvalidUsername() throws Exception {
        String userJson = "{" +
                "\"username\":\"abc\"," +
                "\"password\":\"Test1234\"," +
                "\"email\":\"valid@example.com\"," +
                "\"firstname\":\"Test\"," +
                "\"lastname\":\"User\"" +
                "}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username must have at least 5 characters")));
    }

    /**
     * Test registration fails if password is too short.
     */
    @Test
    @Order(4)
    public void testRegisterUserPasswordTooShort() throws Exception {
        String userJson = "{" +
                "\"username\":\"validUser\"," +
                "\"password\":\"T1a\"," +
                "\"email\":\"valid@example.com\"," +
                "\"firstname\":\"Valid\"," +
                "\"lastname\":\"User\"" +
                "}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Password must be at least 6 characters")));
    }

    /**
     * Test registration fails if password lacks an uppercase letter.
     */
    @Test
    @Order(5)
    public void testRegisterUserPasswordNoUppercase() throws Exception {
        String userJson = "{" +
                "\"username\":\"validUser\"," +
                "\"password\":\"password1\"," +
                "\"email\":\"valid@example.com\"," +
                "\"firstname\":\"Valid\"," +
                "\"lastname\":\"User\"" +
                "}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Password must contain at least one uppercase letter")));
    }

    /**
     * Test registration fails if password lacks a digit.
     */
    @Test
    @Order(6)
    public void testRegisterUserPasswordNoDigit() throws Exception {
        String userJson = "{" +
                "\"username\":\"validUser\"," +
                "\"password\":\"Password\"," +
                "\"email\":\"valid@example.com\"," +
                "\"firstname\":\"Valid\"," +
                "\"lastname\":\"User\"" +
                "}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Password must contain at least one uppercase letter and one number")));
    }

    /**
     * Test registration fails for an invalid email address.
     */
    @Test
    @Order(7)
    public void testRegisterUserInvalidEmail() throws Exception {
        String userJson = "{" +
                "\"username\":\"validUser\"," +
                "\"password\":\"Password1\"," +
                "\"email\":\"invalid-email\"," +
                "\"firstname\":\"Valid\"," +
                "\"lastname\":\"User\"" +
                "}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email address")));
    }

    /**
     * Test that a user can login successfully with valid credentials.
     */
    @Test
    @Order(8)
    public void testLoginUser() throws Exception {
        // Register user with complete fields.
        String userJson = "{" +
                "\"username\":\"loginUser\"," +
                "\"password\":\"Test1234\"," +
                "\"email\":\"login@example.com\"," +
                "\"firstname\":\"Login\"," +
                "\"lastname\":\"User\"" +
                "}";
        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());

        // Login with correct credentials.
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

    /**
     * Test that login fails with wrong credentials.
     */
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
