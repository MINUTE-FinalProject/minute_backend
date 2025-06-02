package com.minute.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeApiService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 1) 키워드 기반 검색 후, 60초 이하 영상(Shorts)만 반환
     *    - 캠핑, 힐링, 산, 테마파크 같은 “카테고리” 단위로 쓰려면 이 메서드를 사용하세요.
     */
    public List<Map<String, Object>> searchVideosByKeyword(String keyword, int maxResults) {
        // ① YouTube Search API 호출 (type=video, part=snippet)
        String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("key", apiKey)
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", maxResults)
                .queryParam("q", keyword)
                .queryParam("regionCode", "KR")
                // ↓ 여행 & 이벤트 카테고리만
                .queryParam("videoCategoryId", "17")
                // ↓ 폭력·음란물 콘텐츠 제외 (moderate 권장, 필요 시 strict)
                .queryParam("safeSearch", "moderate")
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("items")) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // ② 얻은 videoId들만 모아서 contentDetails 조회
        List<String> videoIds = items.stream()
                .map(item -> {
                    Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                    return idMap != null ? (String) idMap.get("videoId") : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (videoIds.isEmpty()) {
            return Collections.emptyList();
        }

        String ids = String.join(",", videoIds);
        String detailUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("key", apiKey)
                .queryParam("part", "contentDetails")
                .queryParam("id", ids)
                .build()
                .toUriString();

        Map<String, Object> detailResp = restTemplate.getForObject(detailUrl, Map.class);
        if (detailResp == null || !detailResp.containsKey("items")) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> details = (List<Map<String, Object>>) detailResp.get("items");

        // ③ 60초 이하인 videoId들만 필터링
        Set<String> shortsIds = details.stream()
                .filter(item -> {
                    Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");
                    String duration = contentDetails != null ? (String) contentDetails.get("duration") : null;
                    int seconds = parseDurationToSeconds(duration);
                    return seconds > 0 && seconds <= 60;
                })
                .map(item -> (String) item.get("id"))
                .collect(Collectors.toSet());

        // ④ 최종적으로 “shortsIds”에 포함된 항목만 반환
        return items.stream()
                .filter(item -> {
                    Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                    String videoId = idMap != null ? (String) idMap.get("videoId") : null;
                    return videoId != null && shortsIds.contains(videoId);
                })
                .collect(Collectors.toList());
    }

    /**
     * 2) 지역 기반 검색 후, 60초 이하(Shorts)만 반환
     *    - “검색 쿼리”가 regionKeyword + " 여행" 형태가 되며, 60초 이하인 결과만 필터합니다.
     *    - 예) searchShortsByRegion("부산", 15) ⇒ “부산 여행” 키워드 검색
     */
    public List<Map<String, Object>> searchShortsByRegion(String regionKeyword, int maxResults) {
        String query = regionKeyword + " 여행";
        // ① YouTube Search API (type=video, part=snippet)
        String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("key", apiKey)
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", maxResults)
                .queryParam("q", query)
                .queryParam("regionCode", "KR")
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("items")) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // ② 얻은 videoId들만 모아서 contentDetails 조회
        List<String> videoIds = items.stream()
                .map(item -> {
                    Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                    return idMap != null ? (String) idMap.get("videoId") : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (videoIds.isEmpty()) {
            return Collections.emptyList();
        }

        String ids = String.join(",", videoIds);
        String detailUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("key", apiKey)
                .queryParam("part", "contentDetails")
                .queryParam("id", ids)
                .build()
                .toUriString();

        Map<String, Object> detailResp = restTemplate.getForObject(detailUrl, Map.class);
        if (detailResp == null || !detailResp.containsKey("items")) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> details = (List<Map<String, Object>>) detailResp.get("items");

        // ③ 60초 이하 비디오 ID만 모으기
        Set<String> shortsIds = details.stream()
                .filter(item -> {
                    Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");
                    String duration = contentDetails != null ? (String) contentDetails.get("duration") : null;
                    int seconds = parseDurationToSeconds(duration);
                    return seconds > 0 && seconds <= 60;
                })
                .map(item -> (String) item.get("id"))
                .collect(Collectors.toSet());

        // ④ 최종적으로 “shortsIds”에 포함된 것만 반환
        return items.stream()
                .filter(item -> {
                    Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                    String videoId = idMap != null ? (String) idMap.get("videoId") : null;
                    return videoId != null && shortsIds.contains(videoId);
                })
                .collect(Collectors.toList());
    }

    /** ISO8601 형식(PnDTnHnMnS)을 초 단위로 변환 */
    private static int parseDurationToSeconds(String iso) {
        if (iso == null) return 0;
        int minutes = 0, seconds = 0;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("PT(?:(\\d+)M)?(?:(\\d+)S)?")
                .matcher(iso);
        if (m.matches()) {
            minutes = m.group(1) != null ? Integer.parseInt(m.group(1)) : 0;
            seconds = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
        }
        return minutes * 60 + seconds;
    }


    @Component
    @RequiredArgsConstructor
    public static class ScheduledVideoFetcher {
        private final YoutubeApiService youtubeApiService;
        private final VideoService videoService;

        /**
         * 6시간마다 “캠핑, 힐링, 산, 테마파크” 네 가지 키워드를 순회하며 DB 저장
         */
        @Scheduled(fixedDelay = 1000 * 60 * 60 * 6)
        public void fetchAllCategoryShorts() {
            List<String> categories = Arrays.asList("캠핑", "힐링", "산", "테마파크");
            for (String category : categories) {
                // 키워드 기반 검색 → 60초 이하만 필터
                List<Map<String, Object>> results = youtubeApiService.searchVideosByKeyword(category, 15);
                // VideoService.saveVideosFromApi(…, category)로 저장 (category field 에 세팅)
                videoService.saveVideosFromApi(results, category);
            }
        }

        /**
         * 6시간마다 지역 기반 숏츠(예: “서울 여행” / “부산 여행”)를 저장하고 싶으면
         * 아래와 같이 추가로 메서드를 만들어도 됩니다.
         * (예시: 매일 오전 3시에 “서울 여행” 키워드로 업데이트)
         */
        @Scheduled(cron = "0 0 3 * * *")
        public void fetchSeoulShorts() {
            List<Map<String, Object>> seoulResults = youtubeApiService.searchShortsByRegion("서울", 15);
            videoService.saveVideosFromApi(seoulResults, "서울");  // category 대신 “서울”로 저장
        }
    }
}
