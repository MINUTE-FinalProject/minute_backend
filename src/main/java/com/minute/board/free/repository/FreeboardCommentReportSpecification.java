package com.minute.board.free.repository.specification;

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

    private static Join<FreeboardCommentReport, User> getReporterJoin(Root<FreeboardCommentReport> root) {
        return root.join("user", JoinType.INNER); // FreeboardCommentReport의 신고자 user 필드
    }

    private static Join<FreeboardComment, FreeboardPost> getOriginalPostJoin(Root<FreeboardCommentReport> root) {
        return getCommentJoin(root).join("freeboardPost", JoinType.INNER);
    }

    // --- Filter and Search Specifications ---

    public static Specification<FreeboardCommentReport> originalPostIdEquals(Integer postId) {
        return (root, query, cb) -> postId == null ? null : cb.equal(getOriginalPostJoin(root).get("postId"), postId);
    }

    public static Specification<FreeboardCommentReport> commentAuthorUserIdEquals(String userId) {
        return (root, query, cb) -> !StringUtils.hasText(userId) ? null : cb.equal(getCommentAuthorJoin(root).get("userId"), userId);
    }

    public static Specification<FreeboardCommentReport> commentAuthorNicknameContains(String nickname) {
        return (root, query, cb) -> !StringUtils.hasText(nickname) ? null : cb.like(cb.lower(getCommentAuthorJoin(root).get("userNickName")), "%" + nickname.toLowerCase() + "%");
    }

    public static Specification<FreeboardCommentReport> reporterUserIdEquals(String userId) {
        return (root, query, cb) -> !StringUtils.hasText(userId) ? null : cb.equal(getReporterJoin(root).get("userId"), userId);
    }

    public static Specification<FreeboardCommentReport> reporterNicknameContains(String nickname) {
        return (root, query, cb) -> !StringUtils.hasText(nickname) ? null : cb.like(cb.lower(getReporterJoin(root).get("userNickName")), "%" + nickname.toLowerCase() + "%");
    }

    public static Specification<FreeboardCommentReport> commentContentContains(String keyword) {
        return (root, query, cb) -> !StringUtils.hasText(keyword) ? null : cb.like(cb.lower(getCommentJoin(root).get("commentContent")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<FreeboardCommentReport> isCommentHidden(Boolean isHidden) {
        return (root, query, cb) -> isHidden == null ? null : cb.equal(getCommentJoin(root).get("commentIsHidden"), isHidden);
    }

    public static Specification<FreeboardCommentReport> reportDateAfter(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("commentReportDate"), startDate.atStartOfDay());
    }

    public static Specification<FreeboardCommentReport> reportDateBefore(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(root.get("commentReportDate"), endDate.atTime(LocalTime.MAX));
    }

    /**
     * 통합 검색 (댓글 내용, 댓글 작성자 닉네임/ID, 신고자 닉네임/ID)
     */
    public static Specification<FreeboardCommentReport> combinedSearch(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String lowerKeyword = "%" + keyword.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(getCommentJoin(root).get("commentContent")), lowerKeyword),
                cb.like(cb.lower(getCommentAuthorJoin(root).get("userNickName")), lowerKeyword),
                cb.like(cb.lower(getCommentAuthorJoin(root).get("userId")), lowerKeyword),
                cb.like(cb.lower(getReporterJoin(root).get("userNickName")), lowerKeyword),
                cb.like(cb.lower(getReporterJoin(root).get("userId")), lowerKeyword)
        );
    }
}