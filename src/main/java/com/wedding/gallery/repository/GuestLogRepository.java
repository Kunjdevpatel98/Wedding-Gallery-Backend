package com.wedding.gallery.repository;

import com.wedding.gallery.model.GuestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GuestLogRepository extends JpaRepository<GuestLog, Long> {
    Optional<GuestLog> findByMobileNumber(String mobileNumber);
}
