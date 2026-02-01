package com.wedding.gallery.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Value("${app.cloudinary.cloud-name}")
    private String cloudName;

    @Value("${app.cloudinary.api-key}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void initialize() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", false));
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        Map params = ObjectUtils.asMap(
                "folder", folder);
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        String url = (String) uploadResult.get("url");
        if (url != null && url.startsWith("https://")) {
            url = url.replace("https://", "http://");
        }
        return url;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        return uploadFile(file, "wedding-folders");
    }

    public void deleteFile(String url) throws IOException {
        String publicId = extractPublicId(url);
        System.out.println("Extracted Public ID: " + publicId + " from URL: " + url);
        if (publicId != null) {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Cloudinary Destroy Result: " + result);
        } else {
            System.err.println("Could not extract Public ID from URL: " + url);
        }
    }

    private String extractPublicId(String url) {
        try {
            // URL Format:
            // https://res.cloudinary.com/cloud-name/image/upload/v12345/folder/filename.jpg
            // OR https://res.cloudinary.com/cloud-name/image/upload/folder/filename.jpg (no
            // version)

            // Logic: Find "upload/" and take everything after it.
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1)
                return null;

            String path = url.substring(uploadIndex + 8); // Skip "/upload/"

            // Remove version "v12345/" if present at the start
            // Regex: starts with v + digits + /
            if (path.matches("^v\\d+/.*")) {
                path = path.replaceFirst("^v\\d+/", "");
            }

            // Remove extension
            int dotIndex = path.lastIndexOf(".");
            if (dotIndex > -1) {
                path = path.substring(0, dotIndex);
            }

            return path;
        } catch (Exception e) {
            System.err.println("Error extracting publicId from URL: " + url);
            e.printStackTrace();
            return null;
        }
    }
}
