package com.minute.video.service;

import com.minute.video.Entity.Tag;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoCategory;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.mapper.VideoMapper;
import com.minute.video.mapper.VideoResponseMapper;
import com.minute.video.repository.SearchHistoryRepository;
import com.minute.video.repository.VideoLikesRepository;
import com.minute.video.repository.VideoRepository;
import com.minute.video.repository.WatchHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VideoService {
    // 영상 조회 및 영상 상세 정보

    private final VideoRepository videoRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final VideoLikesRepository videoLikesRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final VideoMapper videoMapper;
    private final VideoResponseMapper videoResponseMapper;
    private final YoutubeApiService youtubeApiService;

    /* 로그인 여부에 따라 전체 조회 or 추천 영상 조회를 분기 */
    public List<VideoResponseDTO> getVideos(String userId){
        if(userId == null || userId.isBlank()){
            // 1. 비로그인 상태 - 전체 영상 조회
            return getAllVideos();
        }else{
            // 2. 로그인 상태 - 추천 영상 조회
            return getRecommendedVideos(userId);
        }
    }

    // 전체 영상 조회
    public List<VideoResponseDTO> getAllVideos() {
        return videoRepository.findTop50ByOrderByVideoIdDesc().stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    // 추천 영상 조회
    public List<VideoResponseDTO> getRecommendedVideos(String userId) {
        // 1. 이미 본 영상 ID 리스트
        List<String> watchedVideoIds = watchHistoryRepository.findByUserUserIdOrderByWatchedAtDesc(userId).stream()
                .map(history -> history.getVideo().getVideoId())
                .toList();

        // 2. 조회수·좋아요 기반 인기 영상 합치기
        List<Video> topByViews = videoRepository.findTop50ByOrderByViewsDesc();
        List<Video> topByLikes = videoRepository.findTop50ByOrderByLikesDesc();

        // 중복 제거하고 두 리스트 합쳐서 최대 30개 집계
        List<Video> candidates = Stream.concat(topByViews.stream(),topByLikes.stream())
                .distinct()
                .limit(30)
                .collect(Collectors.toList());

        // 3. 검색 키워드
        List<String> keywords = searchHistoryRepository.findByUserUserIdOrderBySearchedAtDesc(userId).stream()
                .map(searchHistory -> searchHistory.getKeyword())
                .toList();

        // 4. 관심 태그
        List<String> favoriteTags = videoLikesRepository.findByUserUserId(userId).stream()
                .flatMap(videoLikes -> videoLikes.getVideo().getVideoTags().stream())
                .map(videoTag -> videoTag.getTag().getTagName())
                .distinct()
                .toList();

        // 4. 필터링, 정렬 , DTO 변환
        return candidates.stream()
                // 1. 이미 본 영상 제외
                .filter(video -> !watchedVideoIds.contains(video.getVideoId()))
                // 2. 관심 태그 매칭 우선
                .sorted(Comparator.comparing((Video video) ->
                                favoriteTags.stream()
                                        .anyMatch(tag -> video.getVideoTags().stream()
                                                .anyMatch(videoTag -> videoTag.getTag().getTagName().equals(tag))))
                        .reversed())
                // 3. 검색 키워드 매칭 우선
                .sorted(Comparator.comparing((Video video) ->
                                keywords.stream()
                                        .anyMatch(keyword -> video.getVideoTitle().contains(keyword)))
                        .reversed())

                // DTO 변환 및 좋아요/조회수 주입
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    // 카테고리별 영상 조회
    public List<VideoResponseDTO> getVideoByCategory(String categoryName) {
        return videoRepository.findByCategoryName(categoryName).stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    // 태그별 영상 조회
    public List<VideoResponseDTO> getVideosByTag(String tagName) {
        return videoRepository.findByTagName(tagName).stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    // 키워드 검색 가능
    public List<VideoResponseDTO> searchByKeyword(String keyword){
        return videoRepository.findByVideoTitleContainingIgnoreCase(keyword)
                .stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    // 영상 상세 조회
    public VideoResponseDTO getVideoDetail(String videoId){
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new EntityNotFoundException("video not found: " + videoId));
        return videoResponseMapper.toDtoWithStats(video);
    }

    // 인기 영상 조회
    /* 좋아요 기준 인기 영상 조회 (fallback: 조회수 → 최신순)*/
    public List<VideoResponseDTO> getPopularByLikeCount(){
        List<Video> videos = videoRepository.findTop50ByOrderByLikesDesc();
        if (videos.isEmpty()){
            // 좋아요 데이터가 없을 때 조회수기준으로 조회
            videos = videoRepository.findTop50ByOrderByViewsDesc();
        }
        if(videos.isEmpty()){
            // 조회수 데이터가 없을 때 videoId기분 최신순
            videos = videoRepository.findTop50ByOrderByVideoIdDesc();
        }
        return videos.stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }
    /* 조회수 기준 인기 영상 조회 (fallback: 최신순)*/
    public List<VideoResponseDTO> getPopularByWatchCount(){
        List<Video> videos = watchHistoryRepository.findMostWatchedVideos();
        if(videos.isEmpty()){
            videos = videoRepository.findTop50ByOrderByViewsDesc();
        }
        if(videos.isEmpty()){
            videos = videoRepository.findTop50ByOrderByVideoIdDesc();
        }
        return videos.stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    // ★ Youtube API로부터 받은 영상 리스트를 DB에 저장
    public void saveVideosFromApi(List<Map<String, Object>> videoList, String region) {
        for (Map<String, Object> videoMap : videoList) {
            Map<String, Object> idMap = (Map<String, Object>) videoMap.get("id");
            Map<String, Object> snippet = (Map<String, Object>) videoMap.get("snippet");
            String videoId = idMap != null ? (String) idMap.get("videoId") : null;
            if (videoId == null || snippet == null) continue;

            // 이미 존재하면 업데이트, 없으면 새로 저장
            Video entity = videoRepository.findById(videoId).orElse(null);

            // 썸네일 url 파싱
            String thumbnailUrl = "";
            if (snippet.get("thumbnails") != null) {
                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                if (thumbnails.get("default") != null) {
                    Map<String, Object> defaultThumb = (Map<String, Object>) thumbnails.get("default");
                    thumbnailUrl = (String) defaultThumb.get("url");
                }
            }

            if (entity == null) {
                // 신규 추가
                entity = Video.builder()
                        .videoId(videoId)
                        .videoTitle((String) snippet.get("title"))
                        .videoDescription((String) snippet.get("description"))
                        .videoUrl("https://www.youtube.com/watch?v=" + videoId)
                        .thumbnailUrl(thumbnailUrl)
                        .region(region)
                        .city("") // 필요하면 추가로 파싱
                        .build();
            } else {
                // 기존 데이터 수정
                entity = Video.builder()
                        .videoId(entity.getVideoId())
                        .videoTitle((String) snippet.get("title"))
                        .videoDescription((String) snippet.get("description"))
                        .videoUrl("https://www.youtube.com/watch?v=" + videoId)
                        .thumbnailUrl(thumbnailUrl)
                        .region(region)
                        .city("") // 필요하면 추가로 파싱
                        .channel(entity.getChannel())
                        .videoCategories(entity.getVideoCategories())
                        .videoTags(entity.getVideoTags())
                        .views(entity.getViews())
                        .likes(entity.getLikes())
                        .build();
            }
            videoRepository.save(entity);
        }
    }

    // 필요시: 지역별 조회, etc
    public List<Video> getVideosByRegion(String region, int limit) {
        return videoRepository.findByRegion(region).stream().limit(limit).toList();
    }

    public List<Video> getVideosByRegionAndCity(String region, String city, int limit) {
        return videoRepository.findByRegionAndCity(region, city).stream().limit(limit).toList();
    }

    public List<Video> getAllVideos(int limit) {
        return videoRepository.findAll().stream().limit(limit).toList();
    }

    public List<VideoResponseDTO> searchByTitleOrRegionOrCity(String keyword) {
        return videoRepository.searchByTitleOrRegionOrCity(keyword).stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    public List<VideoResponseDTO> searchMixedVideos(String keyword, int apiCount) {
        // 1. DB 영상
        List<VideoResponseDTO> dbList = searchByKeyword(keyword);

        // 2. 유튜브 API 영상
        List<Map<String, Object>> apiList = youtubeApiService.searchVideosByKeyword(keyword, apiCount);
        List<VideoResponseDTO> apiDtoList = apiList.stream()
                .map(apiMap -> {
                    Map<String, Object> idMap = (Map<String, Object>) apiMap.get("id");
                    Map<String, Object> snippet = (Map<String, Object>) apiMap.get("snippet");
                    if (idMap == null || snippet == null) return null;
                    String videoId = (String) idMap.get("videoId");
                    String title = (String) snippet.get("title");
                    String description = (String) snippet.get("description");
                    String channelTitle = (String) snippet.get("channelTitle");
                    String url = videoId != null ? "https://www.youtube.com/watch?v=" + videoId : null;
                    String thumbnail = "";
                    if (snippet.get("thumbnails") != null) {
                        Map<String, Object> thumbs = (Map<String, Object>) snippet.get("thumbnails");
                        if (thumbs.get("default") != null) {
                            thumbnail = (String) ((Map<String, Object>) thumbs.get("default")).get("url");
                        }
                    }
                    return VideoResponseDTO.builder()
                            .videoId(videoId)
                            .videoTitle(title)
                            .videoDescription(description)
                            .videoUrl(url)
                            .thumbnailUrl(thumbnail)
                            .channelName(channelTitle)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 3. 두 리스트 합치고, videoId로 중복 제거
        Map<String, VideoResponseDTO> mergedMap = new LinkedHashMap<>();
        for (VideoResponseDTO dto : dbList) mergedMap.put(dto.getVideoId(), dto);
        for (VideoResponseDTO dto : apiDtoList) mergedMap.putIfAbsent(dto.getVideoId(), dto);

        return new ArrayList<>(mergedMap.values());
    }
}
