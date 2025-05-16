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
@Table(name = "freeboard_post_likes",
        uniqueConstraints = { // 복합 유니크 키 설정
                @UniqueConstraint(name = "uk_fpl_user_post", columnNames = {"user_id", "post_id"})
        }
)
public class FreeboardPostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id")
    private Integer postLikeId;

    @CreationTimestamp
    @Column(name = "post_like_created_at", nullable = false, updatable = false)
    private LocalDateTime postLikeCreatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false) // FreeboardPost의 FK
    private FreeboardPost freeboardPost;
}