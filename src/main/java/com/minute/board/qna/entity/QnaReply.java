package com.minute.board.qna.entity;

import com.minute.user.entity.User; // User 엔티티 경로 (답변 작성자)
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inquiry_replies") // DB 테이블명은 'inquiry_replies'
public class QnaReply { // 클래스명은 'QnaReply'

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Integer replyId;

    @Lob
    @Column(name = "reply_content", nullable = false, columnDefinition = "TEXT")
    private String replyContent;

    @CreationTimestamp
    @Column(name = "reply_created_at", nullable = false, updatable = false)
    private LocalDateTime replyCreatedAt;

    @UpdateTimestamp
    @Column(name = "reply_updated_at", nullable = false)
    private LocalDateTime replyUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id") // 답변 작성자 (관리자 User)
    private User user;

    // QnaReply 입장에서 Qna는 하나만 가리킴 (1:1 관계의 주인 쪽, FK를 가짐)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false, unique = true) // 원본 문의 ID, unique = true 중요
    private Qna qna;
}