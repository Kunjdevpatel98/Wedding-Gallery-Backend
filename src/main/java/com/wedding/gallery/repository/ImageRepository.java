package com.wedding.gallery.repository;

import com.wedding.gallery.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByStatus(Image.ImageStatus status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Image i SET i.weddingId = :newId WHERE i.weddingId = :oldId")
    void updateWeddingId(@org.springframework.data.repository.query.Param("oldId") String oldId,
            @org.springframework.data.repository.query.Param("newId") String newId);

    List<Image> findByWeddingId(String weddingId);
}
