package com.wedding.gallery.repository;

import com.wedding.gallery.model.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {
}
