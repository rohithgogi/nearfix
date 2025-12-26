package com.nearfix.nearfix.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage location created at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    public String storeFile(MultipartFile file, String folder) {
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Validate file extension
        String fileExtension = getFileExtension(originalFilename);
        if (!isValidFileType(fileExtension)) {
            throw new RuntimeException("Invalid file type. Only images and PDFs allowed");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 5MB limit");
        }

        try {
            // Generate unique filename
            String newFilename = UUID.randomUUID().toString() + "." + fileExtension;

            // Create folder if doesn't exist
            Path folderPath = this.fileStorageLocation.resolve(folder);
            Files.createDirectories(folderPath);

            // Store file
            Path targetLocation = folderPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative URL
            String fileUrl = "/uploads/" + folder + "/" + newFilename;
            log.info("File stored successfully: {}", fileUrl);
            return fileUrl;

        } catch (IOException ex) {
            log.error("Failed to store file: {}", originalFilename, ex);
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Remove leading /uploads/ from URL
            String relativePath = fileUrl.replace("/uploads/", "");
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", fileUrl);
        } catch (IOException ex) {
            log.warn("Failed to delete file: {}", fileUrl, ex);
        }
    }

    public Resource loadFileAsResource(String fileUrl) {
        try {
            // Remove leading /uploads/ from URL
            String relativePath = fileUrl.replace("/uploads/", "");
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileUrl);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + fileUrl, ex);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isValidFileType(String extension) {
        return extension.matches("jpg|jpeg|png|gif|pdf");
    }
}