/**
 * Controller for handling image operations: upload, retrieval, and deletion.
 */
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

    /**
     * Uploads an image and associates it with the authenticated user.
     *
     * @param file the image file
     * @param authentication the authentication object containing the user
     * @return ResponseEntity with the upload result
     */
    @PostMapping("/upload")
    public ResponseEntity<Map> uploadImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String username = authentication.getName();
        return imageService.uploadImage(file, username);
    }

    /**
     * Retrieves all images associated with the authenticated user.
     *
     * @param authentication the authentication object containing the user
     * @return ResponseEntity with user images
     */
    @GetMapping("/all")
    public ResponseEntity<Map> getUserImages(Authentication authentication) {
        String username = authentication.getName();
        return imageService.getUserImages(username);
    }

    /**
     * Deletes an image identified by delete hash if associated with the authenticated user.
     *
     * @param deleteHash the delete hash for the image
     * @param authentication the authentication object containing the user
     * @return ResponseEntity with deletion result
     */
    @DeleteMapping("/delete/{deleteHash}")
    public ResponseEntity<Map> deleteImage(@PathVariable String deleteHash, Authentication authentication) {
        String username = authentication.getName();
        return imageService.deleteImage(deleteHash, username);
    }
}
