/**
 * Controller handling authentication: user registration and login.
 */
package com.syfproject.img_store.controller;

import com.syfproject.img_store.domain.entity.User;
import com.syfproject.img_store.domain.repository.UserRepository;
import com.syfproject.img_store.dto.LoginRequest;
import com.syfproject.img_store.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Registers a new user.
     *
     * @param user the user data from the request body
     * @return ResponseEntity with registration result
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        log.info("Attempting to register user: {}", user.getUsername());
        try {
            if (userRepository.findByUsername(user.getUsername()).isPresent()){
                log.warn("Registration failed: Username {} is already taken", user.getUsername());
                return ResponseEntity.badRequest().body("Username is already taken");
            }
            // Encode the password before saving.
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            log.info("User {} registered successfully", user.getUsername());
            return ResponseEntity.ok("User registered successfully");
        } catch(Exception e){
            log.error("Unexpected error during registration", e);
            return ResponseEntity.internalServerError().body("Internal server error occurred registering a user");
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param loginRequest the login request containing username and password
     * @return ResponseEntity with JWT token or error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.createToken(loginRequest.getUsername(),
                    Collections.singletonList("USER"));
            log.info("User {} authenticated successfully", loginRequest.getUsername());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
