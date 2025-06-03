package com.minute.bookmark.dto;

import com.minute.bookmark.entity.Bookmark; // Bookmark 엔티티 import
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkResponseDTO {

    private Integer bookmarkId;
    private String videoId;
    private Integer folderId;
    private String userId;
    private String thumbnailUrl;
    private String title;

    // --- 👇 [핵심] 엔티티를 DTO로 변환하는 정적 메소드 ---
    public static BookmarkResponseDTO fromEntity(Bookmark bookmark) {
        if (bookmark == null) return null;

        return BookmarkResponseDTO.builder()
                .bookmarkId(bookmark.getBookmarkId())
                .videoId(bookmark.getVideoId())
                .folderId(bookmark.getFolder() != null ? bookmark.getFolder().getFolderId() : null)
                .userId(bookmark.getUserId())
                .thumbnailUrl(bookmark.getThumbnailUrl())
                .title(bookmark.getTitle()) // DTO의 title 필드에 엔티티의 title 값 매핑
                .build();
    }
}