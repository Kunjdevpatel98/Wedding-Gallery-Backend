package com.wedding.gallery.controller;

import com.wedding.gallery.model.Highlight;
import com.wedding.gallery.repository.HighlightRepository;
import com.wedding.gallery.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/highlights")
@CrossOrigin(origins = "*")
public class HighlightController {

    @Autowired
    private HighlightRepository highlightRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping
    public List<Highlight> getAllHighlights() {
        return highlightRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createHighlight(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            Highlight highlight = new Highlight(imageUrl, title);
            highlightRepository.save(highlight);
            return ResponseEntity.ok(highlight);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload highlight image");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHighlight(@PathVariable Long id) {
        Highlight highlight = highlightRepository.findById(id).orElse(null);
        if (highlight == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            if (highlight.getImageUrl() != null) {
                cloudinaryService.deleteFile(highlight.getImageUrl());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to delete image from Cloudinary");
        }

        highlightRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
