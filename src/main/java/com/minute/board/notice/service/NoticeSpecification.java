package com.minute.board.notice.service;

import com.minute.board.notice.entity.Notice;
import com.minute.user.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class NoticeSpecification {

    /**
     * 검색 유형(searchType)과 검색어(keyword)를 받아 적절한 Specification을 반환합니다.
     *
     * @param searchType 검색할 필드 또는 방식 (예: "title", "content", "author_nickname", "author_id", "all")
     * @param keyword    검색어
     * @return 생성된 Specification 객체, 조건이 없으면 null 대신 항상 참인 조건을 반환할 수도 있습니다.
     * 여기서는 null을 반환하고 서비스에서 null 체크 후 and로 조합합니다.
     */
    public static Specification<Notice> searchByKeyword(final String searchType, final String keyword) {
        // keyword가 비어있거나 공백이면 조건을 적용하지 않고 null 반환 (서비스에서 처리)
        if (!StringUtils.hasText(keyword) || !StringUtils.hasText(searchType)) {
            return null; // 또는 Specification.where(null) 과 같이 항상 참인 조건을 반환해도 됩니다.
        }

        return (Root<Notice> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            // Notice와 User 엔티티를 조인합니다. 검색 조건에 따라 필요할 수 있습니다.
            // N+1 문제를 피하기 위해 fetch join을 고려할 수 있으나, 여기서는 단순 join을 사용합니다.
            // distinct(true)는 join으로 인해 결과가 중복될 경우를 대비해 설정할 수 있습니다.
            // query.distinct(true);

            Join<Notice, User> userJoin = root.join("user", JoinType.LEFT); // LEFT JOIN 사용

            switch (searchType.toLowerCase()) {
                case "title":
                    return cb.like(root.get("noticeTitle"), "%" + keyword + "%");
                case "content": // 내용 검색 조건 추가
                    return cb.like(root.get("noticeContent"), "%" + keyword + "%");
                case "author_nickname":
                    return cb.like(userJoin.get("userNickName"), "%" + keyword + "%");
                case "author_id":
                    return cb.like(userJoin.get("userId"), "%" + keyword + "%");
                case "all": // 제목 OR 내용 OR 작성자 닉네임 OR 작성자 ID
                    Predicate titlePredicate = cb.like(root.get("noticeTitle"), "%" + keyword + "%");
                    Predicate contentPredicate = cb.like(root.get("noticeContent"), "%" + keyword + "%");
                    Predicate nicknamePredicate = cb.like(userJoin.get("userNickName"), "%" + keyword + "%");
                    Predicate userIdPredicate = cb.like(userJoin.get("userId"), "%" + keyword + "%");
                    return cb.or(titlePredicate, contentPredicate, nicknamePredicate, userIdPredicate);
                default:
                    // 유효하지 않은 searchType이거나, 기본 검색을 제목으로 하고 싶을 때
                    return cb.like(root.get("noticeTitle"), "%" + keyword + "%");
            }
        };
    }

    // 만약 개별 조건 Specification이 더 필요하다면 여기에 추가합니다.
    // 예: public static Specification<Notice> titleContains(final String keyword) { ... }
    /**
     * 중요도(isImportant) 필터링을 위한 Specification
     * @param isImportant 필터링할 중요도 값 (true 또는 false). null이면 이 조건은 적용되지 않음.
     * @return Specification 객체
     */
    public static Specification<Notice> isImportant(final Boolean isImportant) {
        return (Root<Notice> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (isImportant == null) {
                return null; // isImportant 파라미터가 없으면 필터링 안 함
            }
            return cb.equal(root.get("noticeIsImportant"), isImportant);
        };
    }

    /**
     * 작성일(createdAt) 날짜 범위 필터링을 위한 Specification
     * @param dateFrom 시작일 (포함). null이면 시작일 제한 없음.
     * @param dateTo 종료일 (포함). null이면 종료일 제한 없음.
     * @return Specification 객체
     */
    public static Specification<Notice> createdAtBetween(final LocalDateTime dateFrom, final LocalDateTime dateTo) {
        return (Root<Notice> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            // 두 날짜 파라미터가 모두 null이면 필터링 안 함
            if (dateFrom == null && dateTo == null) {
                return null;
            }
            // 시작일만 있는 경우 (dateFrom 이후)
            if (dateFrom != null && dateTo == null) {
                return cb.greaterThanOrEqualTo(root.get("noticeCreatedAt"), dateFrom);
            }
            // 종료일만 있는 경우 (dateTo 이전)
            if (dateFrom == null && dateTo != null) {
                // 날짜/시간 비교 시, 종료일을 포함하려면 하루를 더하거나, 시간까지 정확히 지정해야 합니다.
                // 여기서는 LocalDateTime이므로 시간까지 비교합니다.
                // 만약 dateTo의 날짜까지만 포함하고 싶다면, dateTo.plusDays(1).toLocalDate().atStartOfDay() 와 같이 조정하거나
                // cb.lessThan(root.get("noticeCreatedAt"), dateTo.plusDays(1)) 등을 사용할 수 있습니다.
                // 여기서는 입력된 시간까지를 기준으로 합니다.
                return cb.lessThanOrEqualTo(root.get("noticeCreatedAt"), dateTo);
            }
            // 시작일과 종료일이 모두 있는 경우 (dateFrom ~ dateTo 사이)
            return cb.between(root.get("noticeCreatedAt"), dateFrom, dateTo);
        };
    }


}
