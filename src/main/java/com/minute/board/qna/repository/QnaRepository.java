package com.minute.board.qna.repository;

import com.minute.board.qna.entity.Qna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QnaRepository extends JpaRepository<Qna, Integer>, JpaSpecificationExecutor<Qna> {
    // Qna (Inquiry) 엔티티의 ID (inquiryId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    /**
     * 특정 사용자의 문의 목록을 작성일자 내림차순으로 페이징하여 조회합니다. (검색어 없는 경우)
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 Qna 목록
     */
    Page<Qna> findByUser_UserIdOrderByInquiryCreatedAtDesc(String userId, Pageable pageable);

    /**
     * 특정 사용자의 문의 목록 중 제목 또는 내용에 검색어가 포함된 결과를 작성일자 내림차순으로 페이징하여 조회합니다.
     * (JPQL을 사용하여 가독성 및 유연성 확보)
     *
     * @param userId     사용자 ID
     * @param searchTerm 검색어 (제목 또는 내용에서 검색)
     * @param pageable   페이징 정보
     * @return 페이징된 Qna 목록
     */
    @Query("SELECT q FROM Qna q WHERE q.user.userId = :userId AND " +
            "(LOWER(q.inquiryTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "q.inquiryContent LIKE CONCAT('%', :searchTerm, '%')) " + // LOWER() 함수 제거
            "ORDER BY q.inquiryCreatedAt DESC")
    Page<Qna> findByUser_UserIdAndSearchTermOrderByInquiryCreatedAtDesc(
            @Param("userId") String userId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    // --- 관리자 기능 등에서 추가적으로 필요할 수 있는 메서드 예시 (아직 QnaServiceImpl에는 미적용) ---

    /**
     * (관리자용) 모든 문의를 제목 또는 내용 또는 작성자 닉네임에 검색어가 포함된 결과를
     * 작성일자 내림차순으로 페이징하여 조회합니다.
     * 필요에 따라 status 등 다른 필터 조건도 추가할 수 있습니다.
     * 더 복잡한 동적 쿼리는 Specifications 또는 QueryDSL 사용을 고려하세요.
     *
     * @param searchTerm 검색어
     * @param pageable   페이징 정보
     * @return 페이징된 Qna 목록
     */
    // @Query("SELECT q FROM Qna q WHERE " +
    //        "LOWER(q.inquiryTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
    //        "LOWER(q.inquiryContent) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
    //        "LOWER(q.user.userNickName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
    //        "ORDER BY q.inquiryCreatedAt DESC")
    // Page<Qna> findAllBySearchTermOrderByInquiryCreatedAtDesc(
    //         @Param("searchTerm") String searchTerm,
    //         Pageable pageable
    // );

    // (관리자용) 특정 상태의 모든 문의를 조회 (예시)
    // Page<Qna> findByInquiryStatusOrderByInquiryCreatedAtDesc(QnaStatus status, Pageable pageable);
}