package com.minute.video.Entity;

import com.minute.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_likes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLikes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int LikesId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // save() 호출 시점에 createdAt이 자동으로 현재 시간으로 반영
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}