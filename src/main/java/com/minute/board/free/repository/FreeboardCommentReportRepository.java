package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardCommentReport;
import com.minute.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreeboardCommentReportRepository extends JpaRepository<FreeboardCommentReport, Integer> {
    // FreeboardCommentReport 엔티티의 ID (commentReportId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 댓글로 이미 신고했는지 확인하는 메서드
    boolean existsByUserAndFreeboardComment(User user, FreeboardComment freeboardComment);
}