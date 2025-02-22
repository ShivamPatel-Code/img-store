package com.syfproject.img_store.controller;

import com.syfproject.img_store.domain.entity.User;
import com.syfproject.img_store.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(("/api/auth"))
public class AuthController {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        try{
            if (userRepository.findByUsername(user.getUsername()).isPresent()){
                return ResponseEntity.badRequest().body("Username is already taken");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return ResponseEntity.ok("User registered successfully");
        }catch(Exception e){
            log.error("unexpected error occured registering a user");
            return ResponseEntity.internalServerError().body("Internal server error occured registering a user");
        }
    }



}
