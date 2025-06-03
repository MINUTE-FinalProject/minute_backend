package com.minute.bookmark.entity;

import com.minute.folder.entity.Folder;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bookmark", uniqueConstraints = { // 테이블 이름을 'bookmarks'로 변경하는 것을 권장합니다.
        @UniqueConstraint(columnNames = {"user_id", "video_id", "folder_id"})
})
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookmarkId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "video_id", nullable = false)
    private String videoId;

    @Column(nullable = false)
    private String videoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    // --- 여기를 수정했습니다 ---
    @Column(name = "video_title") // DB 컬럼 이름은 'video_title'로 유지
    private String title;          // Java 필드 이름은 'title'로 변경

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
}