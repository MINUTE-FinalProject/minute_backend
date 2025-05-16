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
@Table(name = "freeboard_comment_reports",
        uniqueConstraints = { // 복합 유니크 키 설정
                @UniqueConstraint(name = "uk_fcr_user_comment", columnNames = {"user_id", "comment_id"})
        }
)
public class FreeboardCommentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_report_id")
    private Integer commentReportId;

    @CreationTimestamp
    @Column(name = "comment_report_date", nullable = false, updatable = false)
    private LocalDateTime commentReportDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false) // FreeboardComment의 FK
    private FreeboardComment freeboardComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user;
}