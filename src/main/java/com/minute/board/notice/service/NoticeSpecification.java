package com.minute.board.notice.service;

import com.minute.board.notice.entity.Notice;
import com.minute.user.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class NoticeSpecification {

    // --- 아래 통합 검색 메소드 사용 ---
    /**
     * 여러 필드(제목, 내용, 작성자ID, 작성자 닉네임)에서 키워드로 통합 검색하는 Specification
     * @param keyword 검색어
     * @return Specification 객체
     */
    public static Specification<Notice> searchByCombinedFields(final String keyword) {
        // keyword가 비어있거나 공백이면 조건을 적용하지 않음
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return (Root<Notice> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Join<Notice, User> userJoin = root.join("user", JoinType.LEFT);
            // query.distinct(true); // 조인으로 인해 결과가 중복될 경우

            String lowerKeyword = keyword.toLowerCase(); // 대소문자 구분 없는 검색

            // VARCHAR 타입 필드들은 cb.lower() 사용 가능성이 높음
            Predicate titlePredicate = cb.like(cb.lower(root.get("noticeTitle")), "%" + lowerKeyword + "%");
            Predicate nicknamePredicate = cb.like(cb.lower(userJoin.get("userNickName")), "%" + lowerKeyword + "%");
            Predicate userIdPredicate = cb.like(cb.lower(userJoin.get("userId")), "%" + lowerKeyword + "%");

            // noticeContent (TEXT 타입)에 대한 검색: cb.lower() 없이 검색
            // DB의 utf8mb4_unicode_ci collation이 대소문자 구분 없는 검색을 지원
            Predicate contentPredicate = cb.like(root.get("noticeContent"), "%" + keyword + "%");
            // 또는 일관성을 위해 keyword도 소문자로 했으니 여기도 lowerKeyword 사용
            // Predicate contentPredicate = cb.like(root.get("noticeContent"), "%" + lowerKeyword + "%");

            return cb.or(titlePredicate, contentPredicate, nicknamePredicate, userIdPredicate);
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
