package com.minute.board.free.repository;

import com.minute.board.free.dto.request.AdminReportedCommentFilterDTO; // 유지 (다른 메서드에서 사용)
import com.minute.board.free.dto.response.AdminReportedCommentEntryDTO; // 유지 (다른 메서드에서 사용)
// import com.minute.board.free.dto.response.ReportedCommentEntryDTO; // 필요시 유지
import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardCommentReport;
import com.minute.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // 유지 (JpaSpecificationExecutor 사용)
// import org.springframework.data.jpa.repository.EntityGraph; // 필요시 유지
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List; // List import 추가
import java.util.Set;  // Set import 추가

public interface FreeboardCommentReportRepository extends JpaRepository<FreeboardCommentReport, Integer>, JpaSpecificationExecutor<FreeboardCommentReport> {
    // FreeboardCommentReport 엔티티의 ID (commentReportId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 댓글로 이미 신고했는지 확인하는 메서드
    boolean existsByUserAndFreeboardComment(User user, FreeboardComment freeboardComment);

    @Query("SELECT new com.minute.board.free.dto.response.AdminReportedCommentEntryDTO(" +
            "c.commentId, c.commentContent, u.userId, u.userNickName, c.commentCreatedAt, p.postId, COUNT(r.commentReportId), c.commentIsHidden) " +
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
            "AND (:#{#filter.queryReportStartDate} IS NULL OR r.commentReportDate >= :#{#filter.queryReportStartDate}) " +
            "AND (:#{#filter.queryReportEndDate} IS NULL OR r.commentReportDate < :#{#filter.queryReportEndDate}) " +
            "GROUP BY c.commentId, c.commentContent, u.userId, u.userNickName, c.commentCreatedAt, p.postId, c.commentIsHidden " +
            "ORDER BY COUNT(r.commentReportId) DESC, c.commentCreatedAt DESC")
    Page<AdminReportedCommentEntryDTO> findReportedCommentSummariesWithFilters(
            @Param("filter") AdminReportedCommentFilterDTO filter,
            Pageable pageable
    );

    // <<< 추가된 메소드 (N+1 해결용) >>>
    /**
     * 특정 사용자가 주어진 댓글 ID 목록 중에서 신고한 댓글 ID들을 조회합니다.
     * @param userId 사용자 ID
     * @param commentIds 댓글 ID 목록
     * @return 신고한 댓글 ID의 Set
     */
    @Query("SELECT fcr.freeboardComment.commentId FROM FreeboardCommentReport fcr WHERE fcr.user.userId = :userId AND fcr.freeboardComment.commentId IN :commentIds")
    Set<Integer> findReportedCommentIdsByUserIdAndCommentIdsIn(@Param("userId") String userId, @Param("commentIds") List<Integer> commentIds);
}