package com.minute.board.free.repository;

import com.minute.board.free.dto.request.AdminReportedPostFilterDTO;
import com.minute.board.free.dto.response.ReportedPostEntryDTO;
import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.entity.FreeboardPostReport;
import com.minute.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FreeboardPostReportRepository extends JpaRepository<FreeboardPostReport, Integer>, JpaSpecificationExecutor<FreeboardPostReport> {
    // FreeboardPostReport 엔티티의 ID (postReportId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 게시글로 이미 신고했는지 확인하는 메서드
    boolean existsByUserAndFreeboardPost(User user, FreeboardPost freeboardPost);

//    @Override
//    @EntityGraph(attributePaths = {"user", "freeboardPost", "freeboardPost.user"})
//    Page<FreeboardPostReport> findAll(Specification<FreeboardPostReport> spec, Pageable pageable);


    @Query("SELECT new com.minute.board.free.dto.response.ReportedPostEntryDTO(" +
            "p.postId, p.postTitle, u.userId, u.userNickName, p.postCreatedAt, COUNT(r.postReportId), p.postIsHidden) " +
            "FROM FreeboardPostReport r " +
            "JOIN r.freeboardPost p " +
            "JOIN p.user u " +
            "WHERE (:#{#filter.postId} IS NULL OR p.postId = :#{#filter.postId}) " +
            "AND (:#{#filter.authorUserId} IS NULL OR u.userId LIKE %:#{#filter.authorUserId}%) " +
            "AND (:#{#filter.authorNickname} IS NULL OR u.userNickName LIKE %:#{#filter.authorNickname}%) " +
            "AND (:#{#filter.postTitle} IS NULL OR p.postTitle LIKE %:#{#filter.postTitle}%) " +
            "AND (:#{#filter.searchKeyword} IS NULL OR (" +
            "      p.postTitle LIKE %:#{#filter.searchKeyword}% OR " +
            "      p.postContent LIKE %:#{#filter.searchKeyword}% " +
            ")) " +
            "AND (:#{#filter.isHidden} IS NULL OR p.postIsHidden = :#{#filter.isHidden}) " +
            // 수정된 날짜 조건: 서비스에서 조정한 queryPostStartDate와 queryPostEndDate 사용
            "AND (:#{#filter.queryPostStartDate} IS NULL OR p.postCreatedAt >= :#{#filter.queryPostStartDate}) " +
            "AND (:#{#filter.queryPostEndDate} IS NULL OR p.postCreatedAt <= :#{#filter.queryPostEndDate}) " + // 종료일은 해당일의 끝 시간으로 조정되었으므로 <= 사용
            // 만약 queryPostEndDate를 plusDays(1).atStartOfDay()로 했다면 p.postCreatedAt < :#{#filter.queryPostEndDate} 사용
            "GROUP BY p.postId, p.postTitle, u.userId, u.userNickName, p.postCreatedAt, p.postIsHidden " +
            "ORDER BY COUNT(r.postReportId) DESC, p.postCreatedAt DESC"
    )
    Page<ReportedPostEntryDTO> findReportedPostSummariesWithFilters(
            @Param("filter") AdminReportedPostFilterDTO filter,
            Pageable pageable
    );
}