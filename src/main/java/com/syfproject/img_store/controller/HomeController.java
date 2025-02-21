package com.syfproject.img_store.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to Img Store";
    }

    @GetMapping("/register")
    public String register() {
        return "Register for Img Store";
    }
}
