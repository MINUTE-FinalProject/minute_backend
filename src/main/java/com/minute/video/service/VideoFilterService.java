package com.minute.video.service;

import com.minute.video.Entity.Video;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class VideoFilterService {
    /**
     * 카테고리 이름(소문자) → 해당 카테고리 전용 필터 키워드 규칙
     */
    private final Map<String, FilterRule> ruleMap = new HashMap<>();

    /**
     * 빈이 초기화될 때 (애플리케이션 시작 시) 카테고리별 키워드 규칙을 한 번만 세팅합니다.
     */
    @PostConstruct
    public void init() {
        // 키로 모두 소문자를 사용하도록 변경
        ruleMap.put("캠핑".toLowerCase(Locale.ROOT), new FilterRule(
                /* positive: “캠핑” 관련 핵심 키워드 */
                List.of("캠핑", "캠프", "야영", "글램핑", "오토캠핑", "캠핑장", "캠핑요리", "차박", "백패킹"),
                /* negative: “후기/리뷰/템/광고/ads” 등 단어가 있으면 걸러버림 */
                List.of("리뷰", "후기", "제품", "용품", "광고", "ads", "템", "꿀템")
        ));

        ruleMap.put("힐링".toLowerCase(Locale.ROOT), new FilterRule(
                List.of("힐링", "휴식", "명상", "치유", "산책", "숲속", "자연", "요가", "풍경", "감성", "명소"),
                List.of("리뷰", "후기", "제품", "용품", "광고", "ads", "템", "꿀템")
        ));

        ruleMap.put("산".toLowerCase(Locale.ROOT), new FilterRule(
                List.of("산", "등산", "트레킹", "하이킹", "백패킹", "클라이밍", "등반", "정상", "등산코스"),
                List.of("리뷰", "후기", "제품", "광고", "ads","부산")
        ));

        ruleMap.put("테마파크".toLowerCase(Locale.ROOT), new FilterRule(
                List.of("테마파크", "놀이공원", "롤러코스터", "어트랙션", "디즈니", "에버랜드", "어뮤즈먼트"),
                List.of("리뷰", "후기", "티켓", "광고", "ads")
        ));

        // 필요하다면 다른 카테고리도 같은 형식으로 추가
        // ruleMap.put("여행".toLowerCase(Locale.ROOT), new FilterRule(...));
    }

    /**
     * 주어진 Video가 해당 카테고리에 노출 가능한 콘텐츠인지(true/false) 판별
     */
    public boolean isAllowed(Video video, String categoryName) {
        if (categoryName == null) {
            // 카테고리가 지정되지 않았다면 기본적으로 허용
            return true;
        }

        // 카테고리명을 소문자로 변환하여 키로 사용
        String key = categoryName.toLowerCase(Locale.ROOT);

        // 맵에서 규칙을 찾고, 없으면 기본 허용(true) 반환
        FilterRule rule = ruleMap.get(key);
        if (rule == null) {
            return true;
        }

        String title = video.getVideoTitle() != null
                ? video.getVideoTitle().toLowerCase(Locale.ROOT)
                : "";
        String desc = video.getVideoDescription() != null
                ? video.getVideoDescription().toLowerCase(Locale.ROOT)
                : "";

        // 1) negative 키워드 검사: 하나라도 포함되어 있으면 false
        for (String neg : rule.getNegativeKeywords()) {
            if (title.contains(neg) || desc.contains(neg)) {
                return false;
            }
        }

        // 2) positive 키워드 검사: 하나라도 포함되어 있으면 true
        for (String pos : rule.getPositiveKeywords()) {
            if (title.contains(pos) || desc.contains(pos)) {
                return true;
            }
        }

        // 3) 위 두 조건에 해당하지 않으면 false
        return false;
    }

    /**
     * FilterRule: 특정 카테고리에서 허용/차단할 키워드 목록을 저장하는 내부 클래스
     */
    private static class FilterRule {
        private final List<String> positiveKeywords;
        private final List<String> negativeKeywords;

        public FilterRule(List<String> positiveKeywords, List<String> negativeKeywords) {
            this.positiveKeywords = positiveKeywords;
            this.negativeKeywords = negativeKeywords;
        }

        public List<String> getPositiveKeywords() {
            return positiveKeywords;
        }

        public List<String> getNegativeKeywords() {
            return negativeKeywords;
        }
    }
}
