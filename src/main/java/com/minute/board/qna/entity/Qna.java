package com.minute.board.qna.entity;

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
@Table(name = "inquiries") // DB 테이블명은 'inquiries'
public class Qna { // 클래스명은 'Qna'

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Integer inquiryId;

    @Column(name = "inquiry_title", nullable = false, length = 255)
    private String inquiryTitle;

    @Lob
    @Column(name = "inquiry_content", nullable = false, columnDefinition = "TEXT")
    private String inquiryContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_status", nullable = false)
    @ColumnDefault("'PENDING'") // DB ENUM 기본값: PENDING
    private QnaStatus inquiryStatus = QnaStatus.PENDING;

    @CreationTimestamp
    @Column(name = "inquiry_created_at", nullable = false, updatable = false)
    private LocalDateTime inquiryCreatedAt;

    @UpdateTimestamp
    @Column(name = "inquiry_updated_at", nullable = false)
    private LocalDateTime inquiryUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    private User user;

    // Qna 입장에서 QnaReply는 하나만 가짐 (1:1 관계, QnaReply가 Qna의 FK를 가짐)
    @OneToOne(mappedBy = "qna", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private QnaReply qnaReply;

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QnaAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QnaReport> reports = new ArrayList<>();
}