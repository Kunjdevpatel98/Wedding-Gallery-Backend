package com.wedding.gallery.repository;

import com.wedding.gallery.model.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteConfigRepository extends JpaRepository<SiteConfig, String> {
}
