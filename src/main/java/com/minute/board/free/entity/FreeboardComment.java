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
@Table(name = "freeboard_comments")
public class FreeboardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer commentId;

    @Lob
    @Column(name = "comment_content", nullable = false, columnDefinition = "TEXT")
    private String commentContent;

    @Column(name = "comment_like_count", nullable = false)
    @ColumnDefault("0")
    private int commentLikeCount = 0; // 실제 좋아요 수는 FreeboardCommentLike 테이블 집계를 통해 관리될 수도 있습니다.

    @Column(name = "comment_is_hidden", nullable = false)
    @ColumnDefault("false") // DB 스키마: DEFAULT 0
    private boolean commentIsHidden = false;

    @CreationTimestamp
    @Column(name = "comment_created_at", nullable = false, updatable = false)
    private LocalDateTime commentCreatedAt;

    @UpdateTimestamp
    @Column(name = "comment_updated_at", nullable = false)
    private LocalDateTime commentUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false) // FreeboardPost의 FK
    private FreeboardPost freeboardPost;

    @OneToMany(mappedBy = "freeboardComment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardCommentLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "freeboardComment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardCommentReport> reports = new ArrayList<>();
}