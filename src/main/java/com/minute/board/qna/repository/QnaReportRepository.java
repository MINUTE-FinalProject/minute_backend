package com.minute.board.qna.repository;

import com.minute.board.qna.entity.Qna;
import com.minute.board.qna.entity.QnaReport;
import com.minute.user.entity.User; // User 엔티티 경로
import org.springframework.data.jpa.repository.JpaRepository;
// JpaSpecificationExecutor는 필요하다면 유지, 이 메서드와는 직접 관련 없음
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional; // Optional 임포트

// JpaSpecificationExecutor는 필요에 따라 유지하거나 제거
public interface QnaReportRepository extends JpaRepository<QnaReport, Integer> /*, JpaSpecificationExecutor<QnaReport> */ {

    // 특정 사용자가 특정 QnA를 신고했는지 확인하는 메서드 (추천)
    boolean existsByQnaAndUser(Qna qna, User user);

    // 또는 기존 신고 정보를 가져오고 싶다면 Optional 반환 (위의 anyMatch 대신 사용 가능)
    Optional<QnaReport> findByQnaAndUser(Qna qna, User user);
}