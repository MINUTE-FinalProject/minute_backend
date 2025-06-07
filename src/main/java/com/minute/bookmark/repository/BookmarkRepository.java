package com.minute.bookmark.repository;

import com.minute.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // userId 로 바로 조회해서 videoId 리스트만 반환
    @Query("SELECT b.videoId FROM Bookmark b WHERE b.userId = :userId")
    List<String> findVideoIdsByUserId(@Param("userId") String userId);
}