package com.minute.board.free.repository;

import com.minute.board.free.dto.request.AdminReportedCommentFilterDTO;
import com.minute.board.free.dto.response.AdminReportedCommentEntryDTO;
import com.minute.board.free.dto.response.ReportedCommentEntryDTO;
import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardCommentReport;
import com.minute.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FreeboardCommentReportRepository extends JpaRepository<FreeboardCommentReport, Integer>, JpaSpecificationExecutor<FreeboardCommentReport> {
    // FreeboardCommentReport 엔티티의 ID (commentReportId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 댓글로 이미 신고했는지 확인하는 메서드
    boolean existsByUserAndFreeboardComment(User user, FreeboardComment freeboardComment);

    @Query("SELECT new com.minute.board.free.dto.response.AdminReportedCommentEntryDTO(" +
            "c.commentId, c.commentContent, u.userId, u.userNickName, c.commentCreatedAt, p.postId, COUNT(r.commentReportId), c.commentIsHidden) " + // COUNT(r.id) -> COUNT(r.commentReportId)로 수정했는지 확인
            "FROM FreeboardCommentReport r " +
            "JOIN r.freeboardComment c " +
            "JOIN c.user u " +
            "JOIN c.freeboardPost p " +
            "WHERE (:#{#filter.originalPostId} IS NULL OR p.postId = :#{#filter.originalPostId}) " +
            "AND (:#{#filter.authorUserId} IS NULL OR u.userId LIKE %:#{#filter.authorUserId}%) " +
            "AND (:#{#filter.authorNickname} IS NULL OR u.userNickName LIKE %:#{#filter.authorNickname}%) " +
            "AND (:#{#filter.searchKeyword} IS NULL OR (" +
            "      c.commentContent LIKE %:#{#filter.searchKeyword}% OR " +
            "      u.userId LIKE %:#{#filter.searchKeyword}% OR " +
            "      u.userNickName LIKE %:#{#filter.searchKeyword}%" +
            ")) " +
            "AND (:#{#filter.isHidden} IS NULL OR c.commentIsHidden = :#{#filter.isHidden}) " +
            // 수정된 날짜 조건: 서비스에서 조정한 queryReportStartDate와 queryReportEndDate 사용
            "AND (:#{#filter.queryReportStartDate} IS NULL OR r.commentReportDate >= :#{#filter.queryReportStartDate}) " +
            "AND (:#{#filter.queryReportEndDate} IS NULL OR r.commentReportDate < :#{#filter.queryReportEndDate}) " + // 서비스에서 +1일 해서 넘겼으므로 < 사용
            "GROUP BY c.commentId, c.commentContent, u.userId, u.userNickName, c.commentCreatedAt, p.postId, c.commentIsHidden " +
            "ORDER BY COUNT(r.commentReportId) DESC, c.commentCreatedAt DESC")
    Page<AdminReportedCommentEntryDTO> findReportedCommentSummariesWithFilters(
            @Param("filter") AdminReportedCommentFilterDTO filter,
            Pageable pageable
    );
}