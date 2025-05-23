package com.minute.video.controller;

import com.minute.video.service.YoutubeApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/youtube")
@RequiredArgsConstructor
public class YoutubeVideoController {

    private final YoutubeApiService youtubeApiService;

    // 상단 슬라이더: 한국 여행 관련 인기 영상 (예: "한국 여행지", "Korea travel" 등)
    @GetMapping("/slider")
    public List<Map<String, Object>> getSliderVideos() {
        String keyword = "한국 여행지"; // 혹은 "Korea travel" 등 필요에 따라 키워드 추가 가능
        return youtubeApiService.searchVideosByKeyword(keyword, 10);
    }

    // 지역별 영상: 예) /api/v1/youtube/region?region=강남
    @GetMapping("/region")
    public List<Map<String, Object>> getRegionVideos(@RequestParam String region) {
        String keyword = region + " 여행"; // 예: "강남 여행"
        return youtubeApiService.searchVideosByKeyword(keyword, 5);
    }
}
