package com.minute.board.qna.entity;

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
@Table(name = "inquiry_reports", // DB 테이블명은 'inquiry_reports'
        uniqueConstraints = { // 복합 유니크 키 설정
                @UniqueConstraint(name = "uk_ir_user_inquiry", columnNames = {"user_id", "inquiry_id"})
        }
)
public class QnaReport { // 클래스명은 'QnaReport'

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_report_id")
    private Integer inquiryReportId;

    @CreationTimestamp
    @Column(name = "inquiry_report_date", nullable = false, updatable = false)
    private LocalDateTime inquiryReportDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false) // Qna의 FK
    private Qna qna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user;
}