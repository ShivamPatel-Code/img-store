/**
 * Service responsible for interacting with the Imgur API.
 */
package com.syfproject.img_store.service;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
public class ImgurClientService {

    @Value("${imgur.client-id}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Deletes an image on Imgur using the provided delete hash.
     *
     * @param deleteHash the delete hash for the image
     * @return ResponseEntity with the response from Imgur
     */
    public ResponseEntity<Map> deleteImage(String deleteHash) {
        String url = "https://api.imgur.com/3/image/" + deleteHash;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Client-ID " + clientId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        log.debug("Deleting image from Imgur with deleteHash: {}", deleteHash);
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, Map.class);
    }

    /**
     * Uploads an image to Imgur.
     *
     * @param file the image file to upload
     * @return ResponseEntity with the response from Imgur
     */
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

            log.info("Uploading image '{}' to Imgur", file.getOriginalFilename());
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            log.debug("Imgur response status: {}", response.getStatusCode());
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            log.error("Error uploading image to Imgur", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
