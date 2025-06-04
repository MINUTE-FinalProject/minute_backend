package com.minute.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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

    /** 키워드 기반(60초 이하) Shorts만 반환. 쿼터 초과 등 오류 발생 시 빈 리스트 반환 */
    public List<Map<String, Object>> searchVideosByKeyword(String keyword, int maxResults) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                    .queryParam("key", apiKey)
                    .queryParam("part", "snippet")
                    .queryParam("type", "video")
                    .queryParam("maxResults", maxResults)
                    .queryParam("q", keyword)
                    .queryParam("regionCode", "KR")
                    .queryParam("videoCategoryId", "17")
                    .queryParam("safeSearch", "moderate")
                    .build()
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("items")) return Collections.emptyList();
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items.isEmpty()) return Collections.emptyList();

            List<String> videoIds = items.stream()
                    .map(item -> {
                        Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                        return idMap != null ? (String) idMap.get("videoId") : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (videoIds.isEmpty()) return Collections.emptyList();

            String ids = String.join(",", videoIds);
            String detailUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                    .queryParam("key", apiKey)
                    .queryParam("part", "contentDetails")
                    .queryParam("id", ids)
                    .build()
                    .toUriString();

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
        } catch (RestClientException e) {
            // 유튜브 API 쿼터 초과/오류 시
            return Collections.emptyList();
        }
    }

    /** 지역 기반(60초 이하) Shorts만 반환. 쿼터 초과 등 오류 발생 시 빈 리스트 반환 */
    public List<Map<String, Object>> searchShortsByRegion(String regionKeyword, int maxResults) {
        try {
            String query = regionKeyword + " 여행";
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
            if (response == null || !response.containsKey("items")) return Collections.emptyList();
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items.isEmpty()) return Collections.emptyList();

            List<String> videoIds = items.stream()
                    .map(item -> {
                        Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                        return idMap != null ? (String) idMap.get("videoId") : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (videoIds.isEmpty()) return Collections.emptyList();

            String ids = String.join(",", videoIds);
            String detailUrl = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                    .queryParam("key", apiKey)
                    .queryParam("part", "contentDetails")
                    .queryParam("id", ids)
                    .build()
                    .toUriString();

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
        } catch (RestClientException e) {
            // 유튜브 API 쿼터 초과/오류 시
            return Collections.emptyList();
        }
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
}
