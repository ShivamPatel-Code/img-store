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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {"kafka.enabled=false"})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    public void testRegisterUser() throws Exception {
        String userJson = "{\"username\":\"testuser\",\"password\":\"testpass\",\"email\":\"testuser@example.com\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User registered successfully")));
    }

    @Test
    @Order(2)
    public void testRegisterDuplicateUser() throws Exception {
        String userJson = "{\"username\":\"duplicateUser\",\"password\":\"testpass\",\"email\":\"dup@example.com\"}";
        // First registration should succeed
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());
        // Second registration should return Bad Request
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username is already taken")));
    }

    @Test
    @Order(3)
    public void testLoginUser() throws Exception {
        // Register user
        String userJson = "{\"username\":\"loginUser\",\"password\":\"testpass\",\"email\":\"login@example.com\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());

        // Login with correct credentials
        String loginJson = "{\"username\":\"loginUser\",\"password\":\"testpass\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/login")
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
    @Order(4)
    public void testLoginWithWrongCredentials() throws Exception {
        String loginJson = "{\"username\":\"nonexistent\",\"password\":\"wrongpass\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid username or password")));
    }
}
