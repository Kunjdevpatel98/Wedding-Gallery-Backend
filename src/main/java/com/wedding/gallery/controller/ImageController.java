package com.wedding.gallery.controller;

import com.wedding.gallery.model.Image;
import com.wedding.gallery.repository.ImageRepository;
import com.wedding.gallery.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private CloudinaryService cloudinaryService;



    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        System.out.println("Connection test endpoint hit!");
        return ResponseEntity.ok("Backend Connection Successful!");
    }

    // Admin only - Bulk Upload
    @PostMapping("/admin/upload")
    public ResponseEntity<?> uploadImages(@RequestParam("files") MultipartFile[] files,
            @RequestParam("weddingId") String weddingId) {
        System.out.println(
                "Received bulk upload request for weddingId: " + weddingId + ", file count: " + files.length);
        List<Image> savedImages = new java.util.ArrayList<>();
        StringBuilder errors = new StringBuilder();

        for (MultipartFile file : files) {
            try {
                String folder = "weddings/" + weddingId;
                String url = cloudinaryService.uploadFile(file, folder);

                Image image = new Image();
                image.setFileUrl(url);
                image.setWeddingId(weddingId);
                image.setUploadedAt(LocalDateTime.now());
                image.setStatus(Image.ImageStatus.PROCESSING);

                Image savedImage = imageRepository.save(image);
                savedImages.add(savedImage);
            } catch (IOException e) {
                System.err.println(
                        "IOException during upload for file " + file.getOriginalFilename() + ": " + e.getMessage());
                errors.append("File ").append(file.getOriginalFilename()).append(" failed: ").append(e.getMessage())
                        .append("; ");
            } catch (Exception e) {
                System.err.println("CRITICAL ERROR for file " + file.getOriginalFilename() + ": " + e.getMessage());
                e.printStackTrace(); // PRINT FULL STACK TRACE
                errors.append("File ").append(file.getOriginalFilename()).append(" failed: ").append(e.getMessage())
                        .append("; ");
            }
        }

        if (savedImages.isEmpty() && files.length > 0) {
            return ResponseEntity.internalServerError().body("All uploads failed: " + errors.toString());
        }

        return ResponseEntity.ok(savedImages);
    }

    @PostMapping("/admin/delete")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteImages(@RequestBody List<Long> imageIds) {
        System.out.println("Received delete request for images IDs: " + imageIds);
        int successCount = 0;
        int failCount = 0;
        StringBuilder errorDetails = new StringBuilder();

        for (Long id : imageIds) {
            try {
                System.out.println("Processing delete for image ID: " + id);
                Image image = imageRepository.findById(id).orElse(null);

                if (image != null) {
                    try {
                        System.out.println("Attempting to delete from Cloudinary: " + image.getFileUrl());
                        cloudinaryService.deleteFile(image.getFileUrl());
                        System.out.println("Cloudinary delete successful for: " + image.getFileUrl());
                    } catch (Exception e) {
                        System.err.println("Cloudinary delete failed for ID " + id + " but proceeding with DB delete: " + e.getMessage());
                    }

                    imageRepository.delete(image);
                    successCount++;
                    System.out.println("Deleted from DB: ID " + id);
                } else {
                    System.err.println("CRITICAL: Image ID not found in DB: " + id);
                    failCount++;
                    errorDetails.append("ID ").append(id).append(" not found; ");
                }
            } catch (Exception e) {
                System.err.println("Error deleting image ID " + id + ": " + e.getMessage());
                e.printStackTrace();
                failCount++;
                errorDetails.append("ID ").append(id).append(": ").append(e.getMessage()).append("; ");
            }
        }

        if (successCount > 0) {
            Map<String, Object> result = Map.of(
                "message", "Deleted " + successCount + " images. Failed: " + failCount,
                "successCount", successCount,
                "failCount", failCount,
                "errors", errorDetails.toString()
            );
            return ResponseEntity.ok().body(result);
        } else {
            return ResponseEntity.internalServerError()
                    .body("Failed to delete any images. Errors: " + errorDetails.toString());
        }
    }



    @DeleteMapping("/admin/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        System.out.println("Received single delete request for image ID: " + id);
        try {
            Image image = imageRepository.findById(id).orElse(null);
            if (image != null) {
                try {
                    cloudinaryService.deleteFile(image.getFileUrl());
                } catch (Exception e) {
                    System.err.println("Cloudinary delete failed for ID " + id + ": " + e.getMessage());
                }
                imageRepository.delete(image);
                return ResponseEntity.ok().body(Map.of("message", "Deleted successfully"));
            } else {
                return ResponseEntity.status(404).body("Image ID " + id + " not found");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting ID " + id + ": " + e.getMessage());
        }
    }

    @GetMapping
    public List<Image> getAllImages(@RequestParam(required = false) String weddingId) {
        if (weddingId != null && !weddingId.isEmpty()) {
            return imageRepository.findByWeddingId(weddingId);
        }
        return imageRepository.findAll();
    }
}
