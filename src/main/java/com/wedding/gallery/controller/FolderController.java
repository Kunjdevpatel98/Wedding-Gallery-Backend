package com.wedding.gallery.controller;

import com.wedding.gallery.model.Folder;
import com.wedding.gallery.repository.FolderRepository;
import com.wedding.gallery.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/folders")
@CrossOrigin(origins = "*")
public class FolderController {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping
    public List<Folder> getAllFolders(@RequestParam(required = false) boolean admin) {
        System.out.println("Getting all folders. Admin flag: " + admin);
        if (admin) {
            List<Folder> all = folderRepository.findAll();
            System.out.println("Returning ALL folders (Admin): " + all.size());
            return all;
        }
        List<Folder> visible = folderRepository.findOnlyVisible();
        System.out.println("Returning VISIBLE folders (Guest): " + visible.size());
        return visible;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Folder> getFolderById(@PathVariable Long id) {
        return folderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createFolder(@RequestParam("name") String name, 
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isVisible", defaultValue = "true") boolean isVisible,
            @RequestParam(value = "upcoming", defaultValue = "false") boolean upcoming) {
        if (folderRepository.findByName(name).isPresent()) {
            return ResponseEntity.badRequest().body("Folder with this name already exists");
        }

        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            Folder folder = new Folder(name, imageUrl);
            folder.setVisible(isVisible);
            folder.setUpcoming(upcoming);
            folderRepository.save(folder);
            return ResponseEntity.ok(folder);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload cover image");
        }
    }

    @Autowired
    private com.wedding.gallery.repository.ImageRepository imageRepository;

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> updateFolder(@PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "isVisible", required = false) Boolean isVisible,
            @RequestParam(value = "upcoming", required = false) Boolean upcoming) {

        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        if (!folder.getName().equals(name) && folderRepository.findByName(name).isPresent()) {
            return ResponseEntity.badRequest().body("Folder name already exists");
        }

        try {
            if (file != null && !file.isEmpty()) {
                if (folder.getCoverImageUrl() != null) {
                    cloudinaryService.deleteFile(folder.getCoverImageUrl());
                }
                String imageUrl = cloudinaryService.uploadImage(file);
                folder.setCoverImageUrl(imageUrl);
            }

            if (isVisible != null) {
                folder.setVisible(isVisible);
            }

            if (upcoming != null) {
                folder.setUpcoming(upcoming);
            }

            if (!folder.getName().equals(name)) {
                String oldName = folder.getName();
                folder.setName(name);
                imageRepository.updateWeddingId(oldName, name);
            }

            folderRepository.save(folder);
            return ResponseEntity.ok(folder);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to update folder");
        }
    }

    @PutMapping("/{id}/visibility")
    public ResponseEntity<?> updateFolderVisibility(@PathVariable Long id, @RequestParam boolean isVisible) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        folder.setVisible(isVisible);
        folderRepository.save(folder);
        return ResponseEntity.ok(folder);
    }
}
