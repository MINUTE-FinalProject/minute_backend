package com.minute.user.entity; // User 엔티티의 실제 패키지 경로

// 필요한 다른 엔티티들의 import 문 (User 엔티티가 참조하는 다른 엔티티들)
import com.minute.board.notice.entity.Notice;
import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardPostLike;
import com.minute.board.free.entity.FreeboardPostReport;
import com.minute.board.free.entity.FreeboardCommentLike;
import com.minute.board.free.entity.FreeboardCommentReport;
import com.minute.board.qna.entity.Qna;
import com.minute.board.qna.entity.QnaReply;
import com.minute.board.qna.entity.QnaReport;
// User 관련 Enum import (별도 파일로 분리된 경우)
import com.minute.user.enumpackage.Role;
import com.minute.user.enumpackage.UserStatus;
import com.minute.user.enumpackage.UserGender;

import jakarta.persistence.*;
import lombok.*; // Getter, Setter, Builder, NoArgsConstructor, AllArgsConstructor
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.ColumnDefault; // 기본값 설정을 위해 추가

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder // Builder 패턴 사용 (팀원 코드에 있었으므로 유지)
@NoArgsConstructor // JPA 기본 생성자
@AllArgsConstructor // Builder 패턴과 함께 사용 시 유용
@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "user_pw", nullable = false, length = 100)
    private String userPw;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "user_nickname", nullable = false, length = 100) // 스키마 주석: UNIQUE 제약조건 고려
    private String userNickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @ColumnDefault("'USER'") // DB ENUM 기본값과 유사하게 JPA 레벨에서도 명시 (실제 DB 기본값은 USER)
    private Role role = Role.USER; // 자바 객체 기본값 설정

    @CreationTimestamp // 엔티티 생성 시 자동으로 현재 시간 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티 수정 시 자동으로 현재 시간 저장
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "user_phone", nullable = false, length = 100)
    private String userPhone;

    @Column(name = "user_email", nullable = false, length = 100) // 스키마 주석: UNIQUE 제약조건 고려
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    @ColumnDefault("'N'") // DB ENUM 기본값과 유사하게 JPA 레벨에서도 명시 (실제 DB 기본값은 N)
    private UserStatus userStatus = UserStatus.N; // 자바 객체 기본값 설정

    @Enumerated(EnumType.STRING)
    @Column(name = "user_gender", nullable = false)
    @ColumnDefault("'MALE'") // DB ENUM 기본값과 유사하게 JPA 레벨에서도 명시 (실제 DB 기본값은 MALE)
    private UserGender userGender = UserGender.MALE; // 자바 객체 기본값 설정

    @Column(name = "user_no", nullable = false) // PK가 아니므로 @GeneratedValue 제거
    private Integer userNo;

    @Column(name = "user_report", nullable = false)
    @ColumnDefault("0") // DB 기본값과 동일하게 설정
    private Integer userReport = 0; // 자바 객체 기본값 설정

    // --- 팀원이 편리하게 사용할 연관관계 매핑 ---
    // mappedBy는 각 대상 엔티티(예: Notice)에 있는 User 타입 필드명과 일치해야 합니다.
    // cascade = CascadeType.ALL: User 엔티티의 변경(저장, 삭제 등)이 연관된 엔티티에도 전파됨.
    // orphanRemoval = true: User와의 관계가 끊어진 자식 엔티티는 자동으로 삭제됨. (예: user.getNotices().remove(noticeObject) 시 DB에서도 해당 notice 삭제)
    // fetch = FetchType.LAZY: 연관된 엔티티를 실제로 사용할 때 조회 (성능 최적화)

    // 공지사항 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Lombok Builder 사용 시 초기화 보장
    private List<Notice> notices = new ArrayList<>();

    // 자유게시판 게시글 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardPost> freeboardPosts = new ArrayList<>();

    // 문의 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Qna> qnas = new ArrayList<>();

    // 자유게시판 댓글 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardComment> freeboardComments = new ArrayList<>();

    // (사용자가 누른) 게시글 좋아요 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardPostLike> freeboardPostLikes = new ArrayList<>();

    // (사용자가 신고한) 게시글 신고 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardPostReport> freeboardPostReports = new ArrayList<>();

    // (관리자로서 작성한) 문의 답변 목록
    // 만약 답변 작성자가 항상 User 테이블의 Admin이라면 이 관계가 유효합니다.
    // 아니라면, QnaReply 엔티티에서 User 참조 방식을 다시 고려해야 합니다.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QnaReply> qnaReplies = new ArrayList<>();

    // (사용자가 신고한) 문의 신고 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<QnaReport> qnaReports = new ArrayList<>();

    // (사용자가 누른) 댓글 좋아요 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardCommentLike> freeboardCommentLikes = new ArrayList<>();

    // (사용자가 신고한) 댓글 신고 목록
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FreeboardCommentReport> freeboardCommentReports = new ArrayList<>();

//     --- 편의 메서드 (선택 사항) ---
//     예시: 사용자가 작성한 공지사항 추가
    public void addNotice(Notice notice) {
        this.notices.add(notice);
        notice.setUser(this); // 양방향 연관관계 설정
    }

    public void removeNotice(Notice notice) {
        this.notices.remove(notice);
        notice.setUser(null);
    }

//     (다른 연관관계에 대한 편의 메서드도 유사하게 추가 가능)
//
//     Enum 정의는 별도 파일(com.minute.user.enumpackage.*)로 분리한 것으로 가정
//     public enum Role { USER, ADMIN }
//     public enum UserStatus { N, Y } // 정상, 정지
//     public enum UserGender { MALE, FEMALE }
}