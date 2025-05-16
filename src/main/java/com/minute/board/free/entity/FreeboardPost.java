package com.minute.board.free.entity;

import com.minute.user.entity.User; // User 엔티티 경로
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "freeboard_posts")
public class FreeboardPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer postId;

    @Column(name = "post_title", nullable = false, length = 255)
    private String postTitle;

    @Lob
    @Column(name = "post_content", nullable = false, columnDefinition = "TEXT")
    private String postContent;

    @Column(name = "post_view_count", nullable = false)
    @ColumnDefault("0")
    private int postViewCount = 0;

    @Column(name = "post_like_count", nullable = false)
    @ColumnDefault("0")
    private int postLikeCount = 0; // 실제 좋아요 수는 FreeboardPostLike 테이블 집계를 통해 관리될 수도 있습니다.

    @Column(name = "post_is_hidden", nullable = false)
    @ColumnDefault("false") // DB 스키마: DEFAULT 0
    private boolean postIsHidden = false;

    @CreationTimestamp
    @Column(name = "post_created_at", nullable = false, updatable = false)
    private LocalDateTime postCreatedAt;

    @UpdateTimestamp
    @Column(name = "post_updated_at", nullable = false)
    private LocalDateTime postUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user;

    @OneToMany(mappedBy = "freeboardPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "freeboardPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardPostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "freeboardPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardPostReport> reports = new ArrayList<>();
}