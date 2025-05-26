package com.minute.video.service;

import com.minute.video.Entity.YoutubeVideo;
import com.minute.video.repository.YoutubeVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YoutubeVideoService {
    private final YoutubeVideoRepository repo;

    // 전체 영상 (예: 최신 100개)
    public List<YoutubeVideo> getAllVideos(int limit) {
        return repo.findAll().stream().limit(limit).toList();
    }

    // 지역별
    public List<YoutubeVideo> getVideosByRegion(String region, int limit) {
        return repo.findByRegion(region).stream().limit(limit).toList();
    }

    // 지역+도시별
    public List<YoutubeVideo> getVideosByRegionAndCity(String region, String city, int limit) {
        return repo.findByRegionAndCity(region, city).stream().limit(limit).toList();
    }

    // ▶▶▶▶▶ 저장 로직 추가! ◀◀◀◀◀
    // 유튜브 API에서 받아온 리스트를 DB에 저장
    public void saveYoutubeVideos(List<Map<String, Object>> videoList, String region) {
        for (Map<String, Object> videoMap : videoList) {
            Map<String, Object> idMap = (Map<String, Object>) videoMap.get("id");
            Map<String, Object> snippet = (Map<String, Object>) videoMap.get("snippet");
            String videoId = idMap != null ? (String) idMap.get("videoId") : null;

            // 썸네일 url 파싱
            String thumbnailUrl = "";
            if (snippet != null && snippet.get("thumbnails") != null) {
                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                if (thumbnails.get("default") != null) {
                    Map<String, Object> defaultThumb = (Map<String, Object>) thumbnails.get("default");
                    thumbnailUrl = (String) defaultThumb.get("url");
                }
            }

            if (videoId == null || snippet == null) continue;

            // 중복저장 방지: 이미 있는 영상이면 스킵 (선택)
            if (repo.existsById(videoId)) continue;

            YoutubeVideo entity = YoutubeVideo.builder()
                    .youtubeVideoId(videoId)
                    .title((String) snippet.get("title"))
                    .description((String) snippet.get("description"))
                    .thumbnailUrl(thumbnailUrl)
                    .region(region)
                    .city("") // 필요하면 파싱해서 추가
                    .build();

            repo.save(entity);
        }
    }
}
