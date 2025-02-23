package com.syfproject.img_store.service;

import com.syfproject.img_store.domain.entity.Image;
import com.syfproject.img_store.domain.entity.User;
import com.syfproject.img_store.domain.repository.ImageRepository;
import com.syfproject.img_store.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ImageService {

    private final ImgurClientService imgurClientService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ImageService(ImgurClientService imgurClientService,
                        ImageRepository imageRepository,
                        UserRepository userRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.imgurClientService = imgurClientService;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Uploads an image to Imgur, associates it with the user, and publishes a Kafka event.
    public ResponseEntity<Map> uploadImage(MultipartFile file, String username) {
        ResponseEntity<Map> imgurResponse = imgurClientService.uploadImage(file);
        if (!imgurResponse.getStatusCode().is2xxSuccessful()) {
            return imgurResponse;
        }
        Map<String, Object> responseBody = (Map<String, Object>) imgurResponse.getBody();
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        String imgurId = (String) data.get("id");
        String imageLink = (String) data.get("link");
        String deleteHash = (String) data.get("deletehash");

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
        User user = userOptional.get();

        Image image = new Image();
        image.setImgurId(imgurId);
        image.setLink(imageLink);
        image.setDeleteHash(deleteHash);
        image.setUser(user);
        imageRepository.save(image);

        // Publish event to Kafka with username and image link.
        String eventMessage = String.format("{\"username\":\"%s\", \"imageLink\":\"%s\"}", username, imageLink);
        kafkaTemplate.send("image-uploads", eventMessage);

        return ResponseEntity.ok(Map.of("message", "Image uploaded successfully", "imageLink", imageLink));
    }

    // Retrieves images associated with the authenticated user.
    public ResponseEntity<Map> getUserImages(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = userOptional.get();
        List<Image> images = imageRepository.findByUser(user);
        if (images.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No image is associated with your account"));
        } else {
            return ResponseEntity.ok(Map.of("user", user.getUsername(), "images", images));
        }
    }

    // Deletes an image by deleteHash after verifying ownership.
    public ResponseEntity<Map> deleteImage(String deleteHash, String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
        User user = userOptional.get();
        Optional<Image> imageOptional = imageRepository.findByDeleteHashAndUser(deleteHash, user);
        if (imageOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Image not found or not associated with the user"));
        }
        Image image = imageOptional.get();
        ResponseEntity<Map> deleteResponse = imgurClientService.deleteImage(deleteHash);
        if (!deleteResponse.getStatusCode().is2xxSuccessful()) {
            return deleteResponse;
        }
        imageRepository.delete(image);
        return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
    }
}
