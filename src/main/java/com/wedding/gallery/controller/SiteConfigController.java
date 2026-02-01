package com.wedding.gallery.controller;

import com.wedding.gallery.model.SiteConfig;
import com.wedding.gallery.repository.SiteConfigRepository;
import com.wedding.gallery.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*") // Allow frontend access
public class SiteConfigController {

    @Autowired
    private SiteConfigRepository siteConfigRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // Get config by key
    @GetMapping("/{key}")
    public ResponseEntity<SiteConfig> getConfig(@PathVariable String key) {
        System.out.println("Config GET request for key: " + key);
        Optional<SiteConfig> config = siteConfigRepository.findById(key);
        return config.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Set config (generic)
    @PostMapping
    public ResponseEntity<SiteConfig> setConfig(@RequestBody SiteConfig config) {
        return ResponseEntity.ok(siteConfigRepository.save(config));
    }

    // Special endpoint for Hero Image upload
    @PostMapping("/hero_image")
    public ResponseEntity<?> uploadHeroImage(@RequestParam("file") MultipartFile file) {
        System.out.println("Hero image upload request received. File size: " + (file != null ? file.getSize() : "null"));
        try {
            // Check for existing hero image
            Optional<SiteConfig> existingConfig = siteConfigRepository.findById("hero_image");
            if (existingConfig.isPresent() && existingConfig.get().getValue() != null) {
                cloudinaryService.deleteFile(existingConfig.get().getValue());
            }

            // Upload to Cloudinary (using a dedicated folder or global)
            // Using "site-assets" as folder for organization
            String url = cloudinaryService.uploadFile(file, "site-assets");

            if (url != null) {
                // Save to Config
                SiteConfig heroConfig = new SiteConfig("hero_image", url);
                siteConfigRepository.save(heroConfig);
                return ResponseEntity.ok(heroConfig);
            } else {
                return ResponseEntity.internalServerError().body("Failed to upload image to Cloudinary");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload error: " + e.getMessage());
        }
    }
}
