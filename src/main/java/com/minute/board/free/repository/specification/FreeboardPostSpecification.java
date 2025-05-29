package com.minute.board.free.repository.specification; // specification 패키지 예시

import com.minute.board.free.entity.FreeboardPost;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;

public class FreeboardPostSpecification {

    /**
     * 숨김 처리되지 않은 게시글만 필터링합니다. (postIsHidden = false)
     */
    public static Specification<FreeboardPost> isNotHidden() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("postIsHidden"));
    }

    /**
     * 작성자 ID로 필터링하는 Specification을 반환합니다.
     */
    public static Specification<FreeboardPost> hasAuthor(String authorUserId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("userId"), authorUserId);
    }

    /**
     * 제목에 특정 키워드를 포함하는 Specification을 반환합니다.
     */
    public static Specification<FreeboardPost> titleContains(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("postTitle"), "%" + keyword + "%");
    }

    /**
     * 내용에 특정 키워드를 포함하는 Specification을 반환합니다.
     */
    public static Specification<FreeboardPost> contentContains(String keyword) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("postContent"), "%" + keyword + "%");
    }

    /**
     * 작성자 닉네임에 특정 키워드를 포함하는 Specification을 반환합니다.
     * 이를 위해서는 User 엔티티와 조인이 필요합니다.
     */
    public static Specification<FreeboardPost> authorNicknameContains(String keyword) {
        return (root, query, criteriaBuilder) -> {
            // 명시적 조인 (User 엔티티의 userNickName 필드 검색)
            // Join<FreeboardPost, User> userJoin = root.join("user", JoinType.INNER); // 이미 관계 매핑이 되어 있다면 root.get("user")로 접근 가능
            return criteriaBuilder.like(root.get("user").get("userNickName"), "%" + keyword + "%");
        };
    }

    /**
     * 통합 검색 Specification을 생성합니다. (제목 OR 내용 OR 닉네임)
     */
    public static Specification<FreeboardPost> combinedSearch(String keyword) {
        return Specification.where(titleContains(keyword))
                .or(contentContains(keyword))
                .or(authorNicknameContains(keyword));
    }

    /**
     * 특정 날짜 이후에 작성된 게시글 (해당 날짜 포함)
     */
    public static Specification<FreeboardPost> createdAtAfter(LocalDate startDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("postCreatedAt"), startDate.atStartOfDay());
    }

    /**
     * 특정 날짜 이전에 작성된 게시글 (해당 날짜 포함)
     */
    public static Specification<FreeboardPost> createdAtBefore(LocalDate endDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("postCreatedAt"), endDate.atTime(LocalTime.MAX));
    }
}