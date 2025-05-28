package com.minute.board.free.repository.specification;

// import com.minute.board.free.dto.request.AdminReportFilterDTO; // 이 클래스에서는 직접 사용 안 함
import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.entity.FreeboardPostReport;
import com.minute.user.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
// import java.time.LocalDateTime; // reportDateAfter/Before 등에서 LocalDate를 받으므로 불필요할 수 있음
import java.time.LocalTime;

public class FreeboardPostReportSpecification {

    // Helper Joins
    private static Join<FreeboardPostReport, FreeboardPost> getPostJoin(Root<FreeboardPostReport> root) {
        return root.join("freeboardPost", JoinType.INNER);
    }

    private static Join<FreeboardPost, User> getPostAuthorJoin(Root<FreeboardPostReport> root) {
        return getPostJoin(root).join("user", JoinType.INNER);
    }

    // --- Filter and Search Specifications for FreeboardPostReport ---

    /**
     * 신고된 게시글의 ID로 검색합니다. (AdminReportFilterDTO의 reportedItemId 필드 값을 받음)
     */
    public static Specification<FreeboardPostReport> postIdEquals(Integer postId) { // 메서드 이름 수정 또는 서비스에서 reportedItemIdEquals 호출
        return (root, query, cb) -> postId == null ? null : cb.equal(getPostJoin(root).get("postId"), postId);
    }

    // 신고된 게시글의 작성자 User ID로 검색
    public static Specification<FreeboardPostReport> reportedPostAuthorUserIdEquals(String keyword) {
        return (root, query, cb) -> !StringUtils.hasText(keyword) ? null : cb.equal(getPostAuthorJoin(root).get("userId"), keyword);
    }

    // 신고된 게시글의 작성자 닉네임 포함 검색
    public static Specification<FreeboardPostReport> reportedPostAuthorNicknameContains(String keyword) {
        return (root, query, cb) -> !StringUtils.hasText(keyword) ? null : cb.like(cb.lower(getPostAuthorJoin(root).get("userNickName")), "%" + keyword.toLowerCase() + "%");
    }

    // 신고된 게시글의 제목 포함 검색
    public static Specification<FreeboardPostReport> postTitleContains(String keyword) {
        return (root, query, cb) -> !StringUtils.hasText(keyword) ? null : cb.like(cb.lower(getPostJoin(root).get("postTitle")), "%" + keyword.toLowerCase() + "%");
    }

    // 신고된 게시글의 내용 포함 검색
    public static Specification<FreeboardPostReport> postContentContains(String keyword) {
        return (root, query, cb) -> !StringUtils.hasText(keyword) ? null : cb.like(cb.lower(getPostJoin(root).get("postContent")), "%" + keyword.toLowerCase() + "%");
    }

    // 신고된 게시글의 숨김 상태 필터
    public static Specification<FreeboardPostReport> isPostHidden(Boolean isHidden) {
        return (root, query, cb) -> isHidden == null ? null : cb.equal(getPostJoin(root).get("postIsHidden"), isHidden);
    }

    // 신고일 기준 필터
    public static Specification<FreeboardPostReport> reportDateAfter(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(root.get("postReportDate"), startDate.atStartOfDay());
    }

    public static Specification<FreeboardPostReport> reportDateBefore(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(root.get("postReportDate"), endDate.atTime(LocalTime.MAX));
    }

    // 원본 게시글 작성일 기준 필터
    public static Specification<FreeboardPostReport> originalPostCreatedAtAfter(LocalDate startDate) {
        return (root, query, cb) -> startDate == null ? null : cb.greaterThanOrEqualTo(getPostJoin(root).get("postCreatedAt"), startDate.atStartOfDay());
    }

    public static Specification<FreeboardPostReport> originalPostCreatedAtBefore(LocalDate endDate) {
        return (root, query, cb) -> endDate == null ? null : cb.lessThanOrEqualTo(getPostJoin(root).get("postCreatedAt"), endDate.atTime(LocalTime.MAX));
    }
}