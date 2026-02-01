package com.wedding.gallery.controller;

import com.wedding.gallery.model.GuestLog;
import com.wedding.gallery.repository.GuestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth/guest")
@CrossOrigin(origins = "*")
public class GuestAuthController {

    @Autowired
    private GuestLogRepository guestLogRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String mobile = payload.get("mobile");

        if (name == null || mobile == null) {
            return ResponseEntity.badRequest().body("Name and Mobile are required");
        }

        // Check if user exists
        Optional<GuestLog> existingGuest = guestLogRepository.findByMobileNumber(mobile);

        // CASE 1: RETURNING VERIFIED USER -> DIRECT LOGIN
        if (existingGuest.isPresent() && existingGuest.get().isVerified()) {
            GuestLog guest = existingGuest.get();
            guest.setName(name);
            guest.setLoginTime(LocalDateTime.now());
            guestLogRepository.save(guest);

            return ResponseEntity.ok(Map.of(
                    "status", "login_success",
                    "message", "Welcome back " + guest.getName(),
                    "token", "guest-token-" + guest.getId(),
                    "name", guest.getName()));
        }

        // CASE 2: NEW USER or UNVERIFIED -> SEND OTP
        String otp = String.format("%04d", new Random().nextInt(10000));
        GuestLog guest;

        if (existingGuest.isPresent()) {
            guest = existingGuest.get();
            guest.setName(name);
            guest.setOtp(otp);
            guest.setLoginTime(LocalDateTime.now());
        } else {
            guest = new GuestLog(name, mobile, otp);
        }

        guestLogRepository.save(guest);

        // DEMO MODE ONLY: Log OTP to console and return it
        System.out.println("OTP for " + mobile + ": " + otp);

        return ResponseEntity.ok(Map.of(
                "status", "otp_sent",
                "message", "OTP Sent to " + mobile,
                "otp", otp));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> payload) {
        String mobile = payload.get("mobile");
        String otp = payload.get("otp");

        Optional<GuestLog> guestOpt = guestLogRepository.findByMobileNumber(mobile);

        if (guestOpt.isPresent()) {
            GuestLog guest = guestOpt.get();
            if (guest.getOtp().equals(otp)) {
                guest.setVerified(true);
                guestLogRepository.save(guest);
                return ResponseEntity.ok(Map.of("message", "Login Successful", "token", "guest-token-" + guest.getId(),
                        "name", guest.getName()));
            }
        }

        return ResponseEntity.status(401).body("Invalid OTP");
    }

    @GetMapping("/list")
    public List<GuestLog> getAllGuests() {
        return guestLogRepository.findAll();
    }
}
