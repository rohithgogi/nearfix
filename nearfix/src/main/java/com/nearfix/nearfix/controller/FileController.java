package com.nearfix.nearfix.controller;

import com.nearfix.nearfix.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5175")
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        // Get the full request URI
        String requestURL = request.getRequestURI();

        // Extract the file path after /api/files
        String filePath = requestURL.substring(requestURL.indexOf("/uploads"));

        try {
            Resource resource = fileStorageService.loadFileAsResource(filePath);

            // Determine content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            // Fallback to default content type
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error serving file: {}", filePath, e);
            return ResponseEntity.notFound().build();
        }
    }
}