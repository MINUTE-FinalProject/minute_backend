package com.minute.board.qna.repository;

import com.minute.board.qna.entity.QnaReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaReportRepository extends JpaRepository<QnaReport, Integer> {
    // QnaReport (InquiryReport) 엔티티의 ID (inquiryReportId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.
}