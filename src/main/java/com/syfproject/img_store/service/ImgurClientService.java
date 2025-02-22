package com.syfproject.img_store.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class ImgurClientService {

    // Set your Imgur Client ID in application.properties
    @Value("${imgur.client-id}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();

    // Get image details from Imgur
    public ResponseEntity<Map> getImage(String imageId) {
        String url = "https://api.imgur.com/3/image/" + imageId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Client-ID " + clientId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
    }

    // Delete an image from Imgur using the delete hash
    public ResponseEntity<Map> deleteImage(String deleteHash) {
        String url = "https://api.imgur.com/3/image/" + deleteHash;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Client-ID " + clientId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);
    }

    public ResponseEntity<Map> uploadImage(MultipartFile file) {
        try {
            String url = "https://api.imgur.com/3/image";

            // Convert file to ByteArrayResource
            Resource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // Prepare request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", fileResource);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Client-ID " + clientId);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create request entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Make API call
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
