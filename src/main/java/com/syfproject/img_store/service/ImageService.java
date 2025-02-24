package com.syfproject.img_store.service;

import com.syfproject.img_store.domain.entity.Image;
import com.syfproject.img_store.domain.entity.User;
import com.syfproject.img_store.domain.repository.ImageRepository;
import com.syfproject.img_store.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ImageService {

    private final ImgurClientService imgurClientService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Autowired
    public ImageService(ImgurClientService imgurClientService,
                        ImageRepository imageRepository,
                        UserRepository userRepository,
                        @Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate) {
        this.imgurClientService = imgurClientService;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Uploads an image to Imgur, associates it with the user, and publishes a Kafka event if enabled.
     *
     * @param file the image file
     * @param username the username of the uploader
     * @return ResponseEntity with upload result
     */
    public ResponseEntity<Map> uploadImage(MultipartFile file, String username) {
        log.info("Uploading image for user: {}", username);
        ResponseEntity<Map> imgurResponse = imgurClientService.uploadImage(file);
        if (!imgurResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Imgur upload failed with status: {}", imgurResponse.getStatusCode());
            return imgurResponse;
        }
        Map<String, Object> responseBody = (Map<String, Object>) imgurResponse.getBody();
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        String imgurId = (String) data.get("id");
        String imageLink = (String) data.get("link");
        String deleteHash = (String) data.get("deletehash");

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.error("User not found: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
        User user = userOptional.get();

        Image image = new Image();
        image.setImgurId(imgurId);
        image.setLink(imageLink);
        image.setDeleteHash(deleteHash);
        image.setFilename(file.getOriginalFilename());  // Set the original file name.
        image.setUser(user);
        imageRepository.save(image);
        log.debug("Image saved for user {} with id {}", username, image.getId());

        if (kafkaEnabled && kafkaTemplate != null) {
            String eventMessage = String.format("{\"username\":\"%s\", \"imageLink\":\"%s\"}", username, imageLink);
            kafkaTemplate.send("image-uploads", eventMessage);
            log.info("Published Kafka event: {}", eventMessage);
        } else {
            log.debug("Kafka not enabled; skipping event publication");
        }
        return ResponseEntity.ok(Map.of("message", "Image uploaded successfully", "imageLink", imageLink));
    }

    /**
     * Retrieves images associated with the given user.
     *
     * @param username the username
     * @return ResponseEntity with the user's images
     */
    public ResponseEntity<Map> getUserImages(String username) {
        log.info("Retrieving images for user: {}", username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.error("User not found: {}", username);
            throw new RuntimeException("User not found");
        }
        User user = userOptional.get();
        List<Image> images = imageRepository.findByUser(user);
        if (images.isEmpty()) {
            log.info("No images found for user: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No image is associated with your account"));
        } else {
            log.info("Found {} images for user: {}", images.size(), username);
            return ResponseEntity.ok(Map.of("user", user.getUsername(), "images", images));
        }
    }

    /**
     * Retrieves a specific image by its ID if it is associated with the given user.
     *
     * @param id the image ID
     * @param username the username
     * @return ResponseEntity with the image details or error message
     */
    public ResponseEntity<Map> getImageById(Long id, String username) {
        log.info("Retrieving image with id {} for user {}", id, username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.error("User not found: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
        User user = userOptional.get();
        Optional<Image> imageOptional = imageRepository.findByIdAndUser(id, user);
        if (imageOptional.isEmpty()) {
            log.error("Image with id {} not found for user {}", id, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Image not found"));
        }
        Image image = imageOptional.get();
        log.debug("Image found: {}", image);
        return ResponseEntity.ok(Map.of(
                "id", image.getId(),
                "imgurId", image.getImgurId(),
                "link", image.getLink(),
                "filename", image.getFilename(),
                "deleteHash", image.getDeleteHash()
        ));
    }

    /**
     * Deletes an image by its delete hash if associated with the given user.
     *
     * @param deleteHash the delete hash
     * @param username the username
     * @return ResponseEntity with deletion result
     */
    public ResponseEntity<Map> deleteImage(String deleteHash, String username) {
        log.info("Deleting image with deleteHash {} for user {}", deleteHash, username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.error("User not found: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
        User user = userOptional.get();
        Optional<Image> imageOptional = imageRepository.findByDeleteHashAndUser(deleteHash, user);
        if (imageOptional.isEmpty()) {
            log.error("Image with deleteHash {} not found for user {}", deleteHash, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Image not found or not associated with the user"));
        }
        Image image = imageOptional.get();
        ResponseEntity<Map> deleteResponse = imgurClientService.deleteImage(deleteHash);
        if (!deleteResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Imgur deletion failed with status: {}", deleteResponse.getStatusCode());
            return deleteResponse;
        }
        imageRepository.delete(image);
        log.info("Image with deleteHash {} deleted for user {}", deleteHash, username);
        return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
    }
}
