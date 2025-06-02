package com.minute.video.scheduler;

import com.minute.video.service.VideoService;
import com.minute.video.service.YoutubeApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledVideoFetcher {

    private final YoutubeApiService youtubeApiService;
    private final VideoService videoService;

    /**
     * 6시간마다 네 가지 카테고리(캠핑, 힐링, 산, 테마파크) 키워드로
     * 유튜브에서 영상을 가져와서 VideoService.saveVideosFromApi(...)를 호출합니다.
     */
    @Scheduled(initialDelay = 0,fixedDelay = 1000 * 60 * 60 * 6)  // 6시간마다 실행
    public void fetchAllCategories() {

        log.info("=== ScheduledVideoFetcher: fetchAllCategories 호출 ===");
        // 1) 캠핑 카테고리
        List<Map<String, Object>> campingResults = youtubeApiService.searchVideosByKeyword("캠핑", 15);
        videoService.saveVideosFromApi(campingResults, "캠핑");

        // 2) 힐링 카테고리
        List<Map<String, Object>> healingResults = youtubeApiService.searchVideosByKeyword("힐링", 15);
        videoService.saveVideosFromApi(healingResults, "힐링");

        // 3) 산 카테고리
        List<Map<String, Object>> mountainResults = youtubeApiService.searchVideosByKeyword("산", 15);
        videoService.saveVideosFromApi(mountainResults, "산");

        // 4) 테마파크 카테고리
        List<Map<String, Object>> themeParkResults = youtubeApiService.searchVideosByKeyword("테마파크", 15);
        videoService.saveVideosFromApi(themeParkResults, "테마파크");
    }
}
