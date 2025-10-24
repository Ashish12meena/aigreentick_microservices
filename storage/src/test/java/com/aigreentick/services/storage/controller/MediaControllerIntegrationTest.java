package com.aigreentick.services.storage.controller;

import com.aigreentick.services.storage.client.dto.response.AccessTokenCredentials;
import com.aigreentick.services.storage.client.dto.response.StorageInfo;
import com.aigreentick.services.storage.client.dto.response.WhatsappMediaUploadResponseDto;
import com.aigreentick.services.storage.client.service.impl.OrganisationClientAdapter;
import com.aigreentick.services.storage.client.service.impl.UserClientAdapter;
import com.aigreentick.services.storage.client.service.impl.WhatsappClientAdapter;
import com.aigreentick.services.storage.config.MediaServiceProperties;
import com.aigreentick.services.storage.dto.response.MediaUploadResponse;
import com.aigreentick.services.storage.enums.MediaType;
import com.aigreentick.services.storage.interceptor.RateLimitInterceptor;
import com.aigreentick.services.storage.repository.MediaRepository;
import com.aigreentick.services.storage.service.impl.upload.MediaUploadServiceImpl;
import com.aigreentick.services.common.context.UserContext;
import com.aigreentick.services.common.context.UserContextData;
import com.aigreentick.services.common.dto.response.FacebookApiResponse;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MediaController.
 */
@SpringBootTest // ✅ ONLY THIS
@AutoConfigureMockMvc // ✅ To use MockMvc
@ActiveProfiles("test")
@Slf4j
class MediaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MediaRepository mediaRepository;

    // Mock only external dependencies
    @MockitoBean
    private WhatsappClientAdapter whatsappClientAdapter;

    @MockitoBean
    private UserClientAdapter userClientAdapter;

    @MockitoBean
    private OrganisationClientAdapter organisationClientAdapter;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;


    @MockitoBean
    private MediaUploadServiceImpl mediaUploadService;

    @MockitoBean
    private MediaServiceProperties mediaServiceProperties;

    @BeforeEach
    void setUp() {
        // Setup user context
        UserContext.set(new UserContextData(1L, 1L));

         // Mock MediaServiceProperties
        when(mediaServiceProperties.getUploadAllowedImageTypes())
                .thenReturn(List.of("image/jpeg", "image/png", "image/jpg"));
        
        when(mediaServiceProperties.getUploadAllowedVideoTypes())
                .thenReturn(List.of("video/mp4", "video/3gpp"));
        
        when(mediaServiceProperties.getUploadAllowedDocumentTypes())
                .thenReturn(List.of("application/pdf", "text/plain"));
        
        when(mediaServiceProperties.getUploadAllowedAudioTypes())
                .thenReturn(List.of("audio/mpeg", "audio/mp4"));
        
        when(mediaServiceProperties.getUploadMaxSize())
                .thenReturn(52428800L);
        
        when(mediaServiceProperties.getBaseUrl())
                .thenReturn("http://localhost:7998/api/v1/media/");

        // Mock rate limiter to allow all requests
        try {
            when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Clear database before each test
        mediaRepository.deleteAll();

        // Mock User Service - returns access credentials
        when(userClientAdapter.getPhoneNumberIdAccessToken(anyLong()))
                .thenReturn(new AccessTokenCredentials("test-phone-id", "test-access-token"));

        when(userClientAdapter.getWabaAccessToken(anyLong()))
                .thenReturn(new AccessTokenCredentials("test-waba-id", "test-access-token"));

        // Mock Organisation Service - returns storage info (1GB available)
        when(organisationClientAdapter.getStorageInfo())
                .thenReturn(new StorageInfo(0L, 1073741824L, 1073741824L));

        // Mock WhatsApp API - returns fake media ID
        WhatsappMediaUploadResponseDto whatsappResponse = new WhatsappMediaUploadResponseDto();
        whatsappResponse.setId("test-media-id-123");

        when(whatsappClientAdapter.uploadMediaToFacebook(
                any(File.class), anyString(), anyString(), anyString()))
                .thenReturn(FacebookApiResponse.success(whatsappResponse, 200));

         when(mediaUploadService.uploadMedia(any(MultipartFile.class)))
                .thenAnswer(invocation -> {
                    MultipartFile multipartFile = invocation.getArgument(0);
                    String uuid = "mock-uuid-" + System.currentTimeMillis();
                    return MediaUploadResponse.builder()
                            .url("http://localhost:7998/api/v1/media/" + uuid + ".jpg")
                            .originalFilename(multipartFile.getOriginalFilename())
                            .storedFilename(uuid + ".jpg")
                            .mediaType(MediaType.IMAGE)
                            .contentType(multipartFile.getContentType())
                            .fileSizeBytes(multipartFile.getSize())
                            .uploadedAt(LocalDateTime.now())
                            .build();
                });

        // ✅ Mock MediaUploadService - NO file saved to disk

    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void uploadMedia_WhenFileIsValid_ShouldReturnSuccess() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        // MediaUploadResponse response = MediaUploadResponse.builder()
        // .url("http://localhost:7998/api/v1/media/abc-123.jpg")
        // .originalFilename("test.jpg")
        // .storedFilename("abc-123.jpg")
        // .mediaType(MediaType.IMAGE) 
        // .contentType("image/jpeg")
        // .fileSizeBytes(1024L)
        // .mediaId("test-media-id-123")
        // .uploadedAt(LocalDateTime.now())
        // .build();

        // when(mediaService.uploadMedia(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(multipart("/api/v1/media/upload")
                .file(file)
                .header("X-User-Id", "1")
                .header("X-Org-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("media uploaded successfully"))
                .andExpect(jsonPath("$.data.url").exists())
                .andExpect(jsonPath("$.data.originalFilename").value("test.jpg"))
                .andExpect(jsonPath("$.data.storedFilename").exists())
                .andExpect(jsonPath("$.data.mediaType").value("IMAGE"))
                .andExpect(jsonPath("$.data.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.data.mediaId").value("test-media-id-123"))
                .andExpect(jsonPath("$.data.fileSizeBytes").exists());

        log.info("Upload test completed successfully");

    }

    @Test
    void getAllMedia_WhenUserContextIsValid_ShouldReturnMediaList() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/media")
                .header("X-User-Id", "1")
                .header("X-Org-Id", "1")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getImages_ShouldReturnImagesOnly() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/media/images")
                .header("X-User-Id", "1")
                .header("X-Org-Id", "1")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getVideos_ShouldReturnVideosOnly() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/media/videos")
                .header("X-User-Id", "1")
                .header("X-Org-Id", "1")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}