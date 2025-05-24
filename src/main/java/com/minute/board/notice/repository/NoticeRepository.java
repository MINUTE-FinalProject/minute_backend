package com.minute.board.notice.repository;

import com.minute.board.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <<< JpaSpecificationExecutor 임포트 추가

// JpaSpecificationExecutor<Notice> 를 추가로 상속합니다.
public interface NoticeRepository extends JpaRepository<Notice, Integer>, JpaSpecificationExecutor<Notice> {
    // Notice 엔티티의 ID (noticeId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.
    //
}