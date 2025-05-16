package com.minute.board.notice.entity;

import com.minute.user.entity.User; // User 엔티티 경로
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Integer noticeId;

    @Column(name = "notice_title", nullable = false, length = 255)
    private String noticeTitle;

    @Lob
    @Column(name = "notice_content", nullable = false, columnDefinition = "TEXT")
    private String noticeContent;

    @Column(name = "notice_is_important", nullable = false)
    @ColumnDefault("false") // DB 스키마: DEFAULT 0
    private boolean noticeIsImportant = false;

    @Column(name = "notice_view_count", nullable = false)
    @ColumnDefault("0")
    private int noticeViewCount = 0;

    @CreationTimestamp
    @Column(name = "notice_created_at", nullable = false, updatable = false)
    private LocalDateTime noticeCreatedAt;

    @UpdateTimestamp
    @Column(name = "notice_updated_at", nullable = false)
    private LocalDateTime noticeUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user; // User 엔티티 참조
}