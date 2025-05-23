package com.minute.video.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YoutubeApiService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 유튜브 검색 API로 지역/키워드별 영상 목록 가져오기
    public List<Map<String, Object>> searchVideosByKeyword(String keyword, int maxResults) {
        String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
                .queryParam("key", apiKey)
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", maxResults)
                .queryParam("q", keyword)
                .queryParam("regionCode", "KR")
                .build()
                .toUriString();

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
        return items;
    }
}
