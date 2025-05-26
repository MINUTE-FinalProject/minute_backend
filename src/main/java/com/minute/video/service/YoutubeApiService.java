package com.minute.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    // 일반 영상 (슬라이드, 카드 공용)
    public List<Map<String, Object>> searchVideosByKeyword(String keyword, int maxResults) {
        String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("key", apiKey)
                .queryParam("part", "snippet")
                .queryParam("type", "short")
                .queryParam("maxResults", maxResults)
                .queryParam("q", keyword)
                .queryParam("regionCode", "KR")
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("items")) return Collections.emptyList();
        Object itemsObj = response.get("items");
        if (!(itemsObj instanceof List)) return Collections.emptyList();
        return (List<Map<String, Object>>) itemsObj;
    }

    // 쇼츠 (60초 이하 영상만 필터)
    public List<Map<String, Object>> searchShortsByRegion(String regionKeyword, int maxResults) {
        String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("key", apiKey)
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", maxResults)
                .queryParam("q", regionKeyword + " 여행")
                .queryParam("regionCode", "KR")
                .build().toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("items")) return Collections.emptyList();

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        List<String> videoIds = items.stream()
                .map(item -> {
                    Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                    return idMap != null ? idMap.get("videoId") : null;
                })
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        if (videoIds.isEmpty()) return Collections.emptyList();

        String ids = String.join(",", videoIds);
        String detailUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("key", apiKey)
                .queryParam("part", "contentDetails")
                .queryParam("id", ids)
                .build().toUriString();

        Map<String, Object> detailResp = restTemplate.getForObject(detailUrl, Map.class);
        if (detailResp == null || !detailResp.containsKey("items")) return Collections.emptyList();
        List<Map<String, Object>> details = (List<Map<String, Object>>) detailResp.get("items");

        Set<String> shortsIds = details.stream()
                .filter(item -> {
                    Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");
                    String duration = contentDetails != null ? (String) contentDetails.get("duration") : null;
                    int seconds = parseDurationToSeconds(duration);
                    return seconds > 0 && seconds <= 60;
                })
                .map(item -> (String) item.get("id"))
                .collect(Collectors.toSet());

        return items.stream()
                .filter(item -> {
                    Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                    String videoId = idMap != null ? (String) idMap.get("videoId") : null;
                    return videoId != null && shortsIds.contains(videoId);
                })
                .collect(Collectors.toList());
    }

    // ISO8601 → 초 변환
    private static int parseDurationToSeconds(String iso) {
        if (iso == null) return 0;
        int min = 0, sec = 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("PT(?:(\\d+)M)?(?:(\\d+)S)?").matcher(iso);
        if (m.matches()) {
            min = m.group(1) != null ? Integer.parseInt(m.group(1)) : 0;
            sec = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
        }
        return min * 60 + sec;
    }
}
