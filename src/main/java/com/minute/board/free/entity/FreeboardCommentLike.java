package com.minute.board.free.entity;

import com.minute.user.entity.User; // User 엔티티 경로
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "freeboard_comment_likes",
        uniqueConstraints = { // 복합 유니크 키 설정
                @UniqueConstraint(name = "uk_fcl_user_comment", columnNames = {"user_id", "comment_id"})
        }
)
public class FreeboardCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id")
    private Integer commentLikeId;

    // 스키마 주석: 컬럼명 일관성을 위해 post_like_created_at -> comment_like_created_at (원본은 comment_created_at)
    // DB 스키마에는 comment_created_at 로 되어 있으므로 이를 따름.
    @CreationTimestamp
    @Column(name = "comment_created_at", nullable = false, updatable = false)
    private LocalDateTime commentCreatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false) // FreeboardComment의 FK
    private FreeboardComment freeboardComment;
}