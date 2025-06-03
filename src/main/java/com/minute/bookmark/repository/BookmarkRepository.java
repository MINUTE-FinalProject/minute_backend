package com.minute.bookmark.repository;

import com.minute.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer> {

    Optional<Bookmark> findByUserIdAndVideoIdAndFolder_FolderId(String userId, String videoId, Integer folderId);

    List<Bookmark> findByFolder_FolderIdAndUserIdOrderByBookmarkIdDesc(Integer folderId, String userId);

    @Transactional
    long deleteByFolder_FolderIdAndVideoIdAndUserId(Integer folderId, String videoId, String userId);

    List<Bookmark> findByUserIdOrderByBookmarkIdDesc(String userId);

    Optional<Bookmark> findByBookmarkIdAndUserId(Integer bookmarkId, String userId);
}