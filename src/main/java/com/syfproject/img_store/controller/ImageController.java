package com.syfproject.img_store.controller;

import com.syfproject.img_store.service.ImgurClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImgurClientService imgurClientService;

    public ImageController(ImgurClientService imgurClientService) {
        this.imgurClientService = imgurClientService;
    }

    // Endpoint to get image details
    @GetMapping("/{imageId}")
    public ResponseEntity<Map> getImage(@PathVariable String imageId) {
        ResponseEntity<Map> response = imgurClientService.getImage(imageId);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    // Endpoint to delete an image
    @DeleteMapping("/{deleteHash}")
    public ResponseEntity<Map> deleteImage(@PathVariable String deleteHash) {
        ResponseEntity<Map> response = imgurClientService.deleteImage(deleteHash);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping("/upload")
    public ResponseEntity<Map> uploadImage(@RequestParam("file") MultipartFile file) {
        return imgurClientService.uploadImage(file);
    }
}

