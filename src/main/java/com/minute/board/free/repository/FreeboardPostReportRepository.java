package com.minute.board.free.repository;

import com.minute.board.free.dto.response.ReportedPostEntryDTO;
import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.entity.FreeboardPostReport;
import com.minute.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FreeboardPostReportRepository extends JpaRepository<FreeboardPostReport, Integer> {
    // FreeboardPostReport 엔티티의 ID (postReportId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 게시글로 이미 신고했는지 확인하는 메서드
    boolean existsByUserAndFreeboardPost(User user, FreeboardPost freeboardPost);

    /**
     * 신고된 게시글 목록과 각 게시글의 신고 횟수, 작성자 정보 등을 페이징하여 조회합니다.
     * 신고 횟수가 많은 순으로 정렬합니다. (페이징 객체에서 다른 정렬을 원하면 변경 가능)
     *
     * @param pageable 페이징 정보
     * @return 페이징된 신고 게시글 정보 DTO 목록
     */
    @Query("SELECT new com.minute.board.free.dto.response.ReportedPostEntryDTO(" +
            "p.postId, p.postTitle, u.userId, u.userNickName, p.postCreatedAt, COUNT(r.postReportId), p.postIsHidden) " +
            "FROM FreeboardPostReport r " +
            "JOIN r.freeboardPost p " +
            "JOIN p.user u " +
            "GROUP BY p.postId, p.postTitle, u.userId, u.userNickName, p.postCreatedAt, p.postIsHidden " +
            "ORDER BY COUNT(r.postReportId) DESC, p.postCreatedAt DESC") //  <--- 집계 함수를 직접 사용// 기본 정렬: 신고 많은 순, 다음은 최신글 순
    Page<ReportedPostEntryDTO> findReportedPostSummaries(Pageable pageable);
}