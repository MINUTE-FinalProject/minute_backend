package com.minute.board.qna.repository;

import com.minute.board.qna.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaRepository extends JpaRepository<Qna, Integer> {
    // Qna (Inquiry) 엔티티의 ID (inquiryId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.
}