package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.entity.FreeboardPostReport;
import com.minute.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreeboardPostReportRepository extends JpaRepository<FreeboardPostReport, Integer> {
    // FreeboardPostReport 엔티티의 ID (postReportId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 게시글로 이미 신고했는지 확인하는 메서드
    boolean existsByUserAndFreeboardPost(User user, FreeboardPost freeboardPost);
}