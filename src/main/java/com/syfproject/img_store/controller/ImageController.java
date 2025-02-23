package com.syfproject.img_store.controller;

import com.syfproject.img_store.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    // Upload endpoint – associates image with the authenticated user.
    @PostMapping("/upload")
    public ResponseEntity<Map> uploadImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String username = authentication.getName();
        return imageService.uploadImage(file, username);
    }

    // Endpoint to get all images of the authenticated user.
    @GetMapping
    public ResponseEntity<Map> getUserImages(Authentication authentication) {
        String username = authentication.getName();
        return imageService.getUserImages(username);

    }

    // Delete endpoint – expects a deleteHash. Checks if that image is associated with the user.
    @DeleteMapping("/{deleteHash}")
    public ResponseEntity<Map> deleteImage(@PathVariable String deleteHash, Authentication authentication) {
        String username = authentication.getName();
        return imageService.deleteImage(deleteHash, username);
    }
}
