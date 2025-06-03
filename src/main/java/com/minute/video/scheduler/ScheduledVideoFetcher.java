package com.minute.video.scheduler;

import com.minute.video.service.VideoService;
import com.minute.video.service.YoutubeApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
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

    /**
     * 6시간마다 “지역 → 도시(구·군)” 단위로 숏츠를 저장
     *
     * Region 목록 총 11개, 각 region마다 3개 도시(구·군)를 순회하며
     * "{도시} 여행" 키워드로 숏츠를 가져와 DB에 저장합니다.
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 1000 * 60 * 60 * 6)  // 6시간마다 실행 (초기 지연 10초)
    public void fetchCityShorts() {
        log.info("=== ScheduledVideoFetcher: fetchCityShorts 호출 ===");

        Map<String, List<String>> regionToCities = new HashMap<>();

        // region
        regionToCities.put("경기도", Arrays.asList("가평", "수원", "파주"));
        regionToCities.put("강원도", Arrays.asList("강릉", "속초", "평창"));
        regionToCities.put("전라북도", Arrays.asList("전주", "군산", "남원"));
        regionToCities.put("충청북도", Arrays.asList("단양", "청주", "제천"));
        regionToCities.put("경상남도", Arrays.asList("통영", "거제", "진주"));
        regionToCities.put("전라남도", Arrays.asList("여수", "순천", "담양"));
        regionToCities.put("제주도",   Arrays.asList("서귀포", "성산", "애월"));
        regionToCities.put("서울",     Arrays.asList("강남", "종로", "홍대"));
        regionToCities.put("충청남도", Arrays.asList("태안", "공주", "보령"));
        regionToCities.put("부산",     Arrays.asList("해운대", "광안리", "서면"));
        regionToCities.put("경상북도", Arrays.asList("경주", "안동", "포항"));

        // 각 region → city마다 “{도시} 여행” 키워드로 조회 후 저장
        for (Map.Entry<String, List<String>> entry : regionToCities.entrySet()) {
            String region = entry.getKey();
            for (String city : entry.getValue()) {
                log.info("Fetching shorts for city: {} (region: {})", city,region);
                List<Map<String, Object>> cityResults =
                        youtubeApiService.searchShortsByRegion(city, 15);
                // ← 여기를 saveVideosFromApi가 아닌 saveVideosByRegionAndCity 로 바꿔야 합니다.
                videoService.saveVideosByRegionAndCity(cityResults, region, city);
            }
        }
    }
}
