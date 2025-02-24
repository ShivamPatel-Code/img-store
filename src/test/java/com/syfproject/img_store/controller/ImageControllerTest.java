package com.syfproject.img_store.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syfproject.img_store.service.ImageService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ImageController.class)
@TestPropertySource(properties = {"kafka.enabled=false"})
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

        // Perform the multipart POST request to /api/images/upload with CSRF token.
        mockMvc.perform(multipart("/api/images/upload")
                        .file(file)
                        .with(csrf())
                        .with(request -> { request.setMethod("POST"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Image uploaded successfully")))
                .andExpect(jsonPath("$.imageLink", is("http://imgur.com/fakeImage.jpg")));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testGetUserImages() throws Exception {
        // Configure the ImageService mock to return a fake list of images.
        Map<String, Object> fakeResponse = Map.of(
                "user", "testuser",
                "images", List.of(
                        Map.of(
                                "id", 1,
                                "imgurId", "fakeId",
                                "link", "http://imgur.com/fakeImage.jpg",
                                "deleteHash", "fakeDeleteHash"
                        )
                )
        );
        when(imageService.getUserImages("testuser"))
                .thenReturn(ResponseEntity.ok(fakeResponse));

        // Perform the GET request to retrieve images.
        mockMvc.perform(get("/api/images/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user", is("testuser")))
                .andExpect(jsonPath("$.images", hasSize(1)))
                .andExpect(jsonPath("$.images[0].link", is("http://imgur.com/fakeImage.jpg")));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testDeleteImage() throws Exception {
        // Configure the ImageService mock to simulate a successful deletion using deleteHash.
        when(imageService.deleteImage("fakeDeleteHash", "testuser"))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Image deleted successfully")));

        // Perform the DELETE request with a CSRF token.
        mockMvc.perform(delete("/api/images/delete/fakeDeleteHash").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Image deleted successfully")));
    }
}
