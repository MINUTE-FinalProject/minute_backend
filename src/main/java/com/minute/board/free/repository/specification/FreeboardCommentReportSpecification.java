package com.minute.board.free.repository.specification;

import com.minute.board.free.dto.request.AdminReportFilterDTO; // 필터 DTO 경로 확인
import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardCommentReport;
import com.minute.board.free.entity.FreeboardPost;
import com.minute.user.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class FreeboardCommentReportSpecification {

    // Helper Joins
    private static Join<FreeboardCommentReport, FreeboardComment> getCommentJoin(Root<FreeboardCommentReport> root) {
        return root.join("freeboardComment", JoinType.INNER);
    }

    private static Join<FreeboardComment, User> getCommentAuthorJoin(Root<FreeboardCommentReport> root) {
        return getCommentJoin(root).join("user", JoinType.INNER);
    }

    private static Join<FreeboardComment, FreeboardPost> getOriginalPostJoin(Root<FreeboardCommentReport> root) {
        return getCommentJoin(root).join("freeboardPost", JoinType.INNER);
    }

    // --- Filter and Search Specifications for FreeboardCommentReport ---

    // 신고된 댓글의 ID로 검색 (AdminReportFilterDTO의 reportedItemId 필드 사용)
    public static Specification<FreeboardCommentReport> commentIdEquals(Integer itemId) {
        return (root, query, cb) -> itemId == null ? null : cb.equal(getCommentJoin(root).get("commentId"), itemId);
    }

    // 댓글이 달린 원본 게시글 ID로 검색 (AdminReportFilterDTO에 originalPostId 필드가 있다면 사용)
    // AdminReportFilterDTO에는 originalPostId 필드가 없으므로, 필요시 DTO에 추가하고 이 메서드를 사용합니다.
    // public static Specification<FreeboardCommentReport> originalPostIdEquals(Integer postId) {
    //    return (root, query, cb) -> postId == null ? null : cb.equal(getOriginalPostJoin(root).get("postId"), postId);
    // }

    // 신고된 댓글의 작성자 User ID로 검색 (AdminReportFilterDTO의 authorKeyword 필드 사용)
    public static Specification<FreeboardCommentReport> commentAuthorUserIdEquals(String keyword) {
        return (root, query, cb) -> !StringUtils.hasText(keyword) ? null : cb.equal(getCommentAuthorJoin(root).get("userId"), keyword);
    }

    // 신고된 댓글의 작성자 닉네임 포함 검색 (AdminReportFilterDTO의 authorKeyword 필드 사용)
    public static Specification<FreeboardCommentReport> commentAuthorNicknameContains(String keyword) {
        return (root, query, cb) -> !StringUtils.hasText(keyword) ? null : cb.like(cb.lower(getCommentAuthorJoin(root).get("userNickName")), "%" + keyword.toLowerCase() + "%");
    }

    // 신고된 댓글의 내용 포함 검색 (AdminReportFilterDTO의 keyword 필드 사용)
    public static Specification<FreeboardCommentReport> commentContentContains(String keyword) {
        return (root, query, cb) -> !StringUtils.hasText(keyword) ? null : cb.like(cb.lower(getCommentJoin(root).get("commentContent")), "%" + keyword.toLowerCase() + "%");
    }

    // 신고된 댓글의 숨김 상태 필터 (AdminReportFilterDTO의 isItemHidden 필드 사용)
    public static Specification<FreeboardCommentReport> isCommentHidden(Boolean isHidden) {
        return (root, query, cb) -> isHidden == null ? null : cb.equal(getCommentJoin(root).get("commentIsHidden"), isHidden);
    }

    // 신고일 기준 필터 (AdminReportFilterDTO의 reportStartDate, reportEndDate 필드 사용)
    public static Specification<FreeboardCommentReport> reportDateAfter(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("commentReportDate"), startDate.atStartOfDay());
    }

    public static Specification<FreeboardCommentReport> reportDateBefore(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(root.get("commentReportDate"), endDate.atTime(LocalTime.MAX));
    }

    // 원본 댓글 작성일 기준 필터 (AdminReportFilterDTO의 originalItemStartDate, originalItemEndDate 필드 사용)
    public static Specification<FreeboardCommentReport> originalCommentCreatedAtAfter(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(getCommentJoin(root).get("commentCreatedAt"), startDate.atStartOfDay());
    }

    public static Specification<FreeboardCommentReport> originalCommentCreatedAtBefore(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(getCommentJoin(root).get("commentCreatedAt"), endDate.atTime(LocalTime.MAX));
    }
}