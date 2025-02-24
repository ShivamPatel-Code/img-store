package com.syfproject.img_store.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syfproject.img_store.service.ImageService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ImageController.class)
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ImageService imageService;

    @Test
    @WithMockUser(username = "testuser")
    public void testUploadImage() throws Exception {
        // Create a dummy file for upload.
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy content".getBytes());

        // Configure the ImageService mock to simulate a successful upload.
        when(imageService.uploadImage(file, "testuser"))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "message", "Image uploaded successfully",
                        "imageLink", "http://imgur.com/fakeImage.jpg"
                )));

        // Perform the multipart POST request with a CSRF token.
        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .with(csrf())
                        .with(request -> { request.setMethod("POST"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Image uploaded successfully")))
                .andExpect(jsonPath("$.imageLink", is("http://imgur.com/fakeImage.jpg")));
    }

    // Test: Invalid file type upload.
    @Test
    @WithMockUser(username = "testuser")
    public void testUploadInvalidFileType() throws Exception {
        // Create a dummy text file (non-image)
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not an image".getBytes());

        // Expect the service to return a BAD_REQUEST error due to invalid file type.
        when(imageService.uploadImage(file, "testuser"))
                .thenReturn(ResponseEntity.status(400)
                        .body(Map.of("error", "Invalid file type. Only image files (jpg, jpeg, png, apng, gif, tiff) are allowed")));

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .with(csrf())
                        .with(request -> { request.setMethod("POST"); return request; }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid file type. Only image files (jpg, jpeg, png, apng, gif, tiff) are allowed")));
    }

    // Test: File larger than 10MB.
    @Test
    @WithMockUser(username = "testuser")
    public void testUploadLargeFile() throws Exception {
        // Create a dummy file that exceeds 10 MB.
        byte[] largeContent = new byte[10 * 1024 * 1024 + 1];  // 10MB + 1 byte
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeContent);

        when(imageService.uploadImage(file, "testuser"))
                .thenReturn(ResponseEntity.status(400)
                        .body(Map.of("error", "File size exceeds the maximum limit of 10 MB")));

        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .with(csrf())
                        .with(request -> { request.setMethod("POST"); return request; }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("File size exceeds the maximum limit of 10 MB")));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetUserImagesEmpty() throws Exception {
        // Configure the ImageService mock to simulate no images found.
        when(imageService.getUserImages("testuser"))
                .thenReturn(ResponseEntity.status(404).body(Map.of("message", "No image is associated with your account")));

        // Perform the GET request.
        mockMvc.perform(get("/api/images/all"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("No image is associated with your account")));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteImageNotFound() throws Exception {
        // Configure the ImageService mock to simulate deletion failure.
        when(imageService.deleteImage("nonexistentHash", "testuser"))
                .thenReturn(ResponseEntity.status(404).body(Map.of("error", "Image not found or not associated with the user")));

        // Perform the DELETE request with a CSRF token.
        mockMvc.perform(delete("/api/images/delete/nonexistentHash").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Image not found or not associated with the user")));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteImageSuccess() throws Exception {
        // Configure the ImageService mock to simulate successful deletion.
        when(imageService.deleteImage("fakeDeleteHash", "testuser"))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Image deleted successfully")));

        // Perform the DELETE request with a CSRF token.
        mockMvc.perform(delete("/api/images/delete/fakeDeleteHash").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Image deleted successfully")));
    }
}
