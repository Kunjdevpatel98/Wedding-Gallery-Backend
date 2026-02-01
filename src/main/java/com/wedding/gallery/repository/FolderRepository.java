package com.wedding.gallery.repository;

import com.wedding.gallery.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findByName(String name);

    java.util.List<Folder> findByVisibleTrue();

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Folder f WHERE f.visible = true OR f.upcoming = true")
    java.util.List<Folder> findOnlyVisible();
}
