package com.minute.bookmark.repository;

import com.minute.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer> {
    Optional<Bookmark> findByUserIdAndVideoIdAndFolder_FolderId(String userId, String videoId, Integer folderId);
}