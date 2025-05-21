package com.minute.folder.repository;

import com.minute.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Integer> {
    List<Folder> findByFolderNameStartingWith(String prefix);
}