package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardPostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreeboardPostReportRepository extends JpaRepository<FreeboardPostReport, Integer> {
    // FreeboardPostReport 엔티티의 ID (postReportId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.
}