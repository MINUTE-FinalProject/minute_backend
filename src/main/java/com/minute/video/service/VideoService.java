package com.minute.video.service;

import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.video.Entity.Category;
import com.minute.video.Entity.Tag;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoCategory;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.mapper.VideoResponseMapper;
import com.minute.video.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final VideoLikesRepository videoLikesRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final VideoResponseMapper videoResponseMapper;
    private final BookmarkRepository bookmarkRepository;
    private final YoutubeApiService youtubeApiService;
    private final CategoryRepository categoryRepository;

    private static final int RECOMMEND_SIZE = 10;

    /* 로그인 여부에 따라 전체 조회 or 추천 영상 조회를 분기 */
    public List<VideoResponseDTO> getVideos(String userId){
        if(userId == null || userId.isBlank()){
            return getAllVideos();
        }else{
            return getRecommendedVideos(userId);
        }
    }

    // 전체 영상 조회
    public List<VideoResponseDTO> getAllVideos() {
        return videoRepository.findTop50ByOrderByVideoIdDesc().stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    // 로그인 사용자를 위한 추천 영상 목록
    public List<VideoResponseDTO> getRecommendedVideos(String userId) {
        List<String> watchedVideoIds = watchHistoryRepository
                .findByUserUserIdOrderByWatchedAtDesc(userId)
                .stream()
                .map(history -> history.getVideo().getVideoId())
                .toList();

        List<String> likedVideoIds = videoLikesRepository
                .findByUserUserId(userId)
                .stream()
                .map(like -> like.getVideo().getVideoId())
                .distinct()
                .toList();

        List<String> favoriteTags = videoLikesRepository
                .findByUserUserId(userId)
                .stream()
                .flatMap(videoLikes -> videoLikes.getVideo().getVideoTags().stream())
                .map(videoTag -> videoTag.getTag().getTagName())
                .distinct()
                .toList();

        List<String> keywords = searchHistoryRepository
                .findByUserUserIdOrderBySearchedAtDesc(userId)
                .stream()
                .map(searchHistory -> searchHistory.getKeyword())
                .toList();

        List<String> bookmarkedVideoIds = bookmarkRepository.findAll().stream()
                .filter(bookmark -> bookmark.getUserId().equals(userId))
                .map(bookmark -> bookmark.getVideoId())
                .distinct()
                .toList();

        List<Video> topByViews = videoRepository.findTop50ByOrderByViewsDesc();
        List<Video> topByLikes = videoRepository.findTop50ByOrderByLikesDesc();
        List<Video> candidates = Stream.concat(topByViews.stream(), topByLikes.stream())
                .distinct()
                .limit(50)
                .collect(Collectors.toList());

        List<Video> scoredList = candidates.stream()
                .filter(video -> !watchedVideoIds.contains(video.getVideoId()))
                .sorted(Comparator.comparing((Video video) -> {
                    int score = 0;
                    if (likedVideoIds.contains(video.getVideoId())) score += 5;
                    if (bookmarkedVideoIds.contains(video.getVideoId())) score += 4;

                    for (String tag : favoriteTags) {
                        boolean match = video.getVideoTags().stream()
                                .anyMatch(videoTag -> videoTag.getTag().getTagName().equalsIgnoreCase(tag));
                        if (match) {
                            score += 3;
                            break;
                        }
                    }

                    for (String keyword : keywords) {
                        if (video.getVideoTitle().toLowerCase().contains(keyword.toLowerCase())) {
                            score += 2;
                            break;
                        }
                    }

                    return score;
                }).reversed())
                .collect(Collectors.toList());

        List<Video> topRecommended = scoredList.stream()
                .limit(RECOMMEND_SIZE)
                .collect(Collectors.toList());

        if (topRecommended.size() < RECOMMEND_SIZE) {
            Set<String> excludeIds = topRecommended.stream()
                    .map(Video::getVideoId)
                    .collect(Collectors.toSet());
            excludeIds.addAll(watchedVideoIds);

            int remaining = RECOMMEND_SIZE - topRecommended.size();

            List<Video> filler = videoRepository.findTop50ByOrderByViewsDesc().stream()
                    .filter(v -> !excludeIds.contains(v.getVideoId()))
                    .limit(remaining)
                    .collect(Collectors.toList());

            topRecommended.addAll(filler);
        }

        return topRecommended.stream()
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

    // 좋아요 기준 인기 영상 조회 (fallback: 조회수 → 최신순)
    public List<VideoResponseDTO> getPopularByLikeCount(){
        List<Video> videos = videoRepository.findTop50ByOrderByLikesDesc();
        if (videos.isEmpty()){
            videos = videoRepository.findTop50ByOrderByViewsDesc();
        }
        if(videos.isEmpty()){
            videos = videoRepository.findTop50ByOrderByVideoIdDesc();
        }
        return videos.stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }
    // 조회수 기준 인기 영상 조회 (fallback: 최신순)
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

    // Youtube API로부터 받은 영상 리스트를 DB에 저장
    @Transactional
    public void saveVideosFromApi(List<Map<String, Object>> videoList, String categoryName) {
        Category category = categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리: " + categoryName));

        for (Map<String, Object> videoMap : videoList) {
            Map<String, Object> idMap      = (Map<String, Object>) videoMap.get("id");
            Map<String, Object> snippetMap = (Map<String, Object>) videoMap.get("snippet");
            if (idMap == null || snippetMap == null) continue;

            String videoId = (String) idMap.get("videoId");
            if (videoId == null) continue;

            Video video = videoRepository.findById(videoId).orElse(null);
            String title       = (String) snippetMap.get("title");
            String description = (String) snippetMap.get("description");
            String videoUrl    = "https://www.youtube.com/watch?v=" + videoId;

            String thumbnailUrl = "";
            Map<String, Object> thumbs = (Map<String,Object>) snippetMap.get("thumbnails");
            if (thumbs != null && thumbs.get("default") != null) {
                thumbnailUrl = (String) ((Map<String,Object>)thumbs.get("default")).get("url");
            }

            if (video == null) {
                video = Video.builder()
                        .videoId(videoId)
                        .videoTitle(title)
                        .videoDescription(description)
                        .videoUrl(videoUrl)
                        .thumbnailUrl(thumbnailUrl)
                        .region("")
                        .city("")
                        .build();
            } else {
                video = Video.builder()
                        .videoId(video.getVideoId())
                        .videoTitle(title)
                        .videoDescription(description)
                        .videoUrl(videoUrl)
                        .thumbnailUrl(thumbnailUrl)
                        .region(video.getRegion())
                        .city(video.getCity())
                        .channel(video.getChannel())
                        .videoCategories(video.getVideoCategories())
                        .videoTags(video.getVideoTags())
                        .views(video.getViews())
                        .likes(video.getLikes())
                        .build();
            }
            videoRepository.save(video);

            VideoCategory.VideoCategoryId vcId =
                    new VideoCategory.VideoCategoryId(videoId, category.getCategoryId());
            boolean alreadyLinked = video.getVideoCategories() != null &&
                    video.getVideoCategories().stream()
                            .anyMatch(vcat -> vcat.getId().equals(vcId));
            if (!alreadyLinked) {
                VideoCategory vcat = VideoCategory.builder()
                        .id(vcId)
                        .video(video)
                        .category(category)
                        .build();

                if (video.getVideoCategories() == null) {
                    video.setVideoCategories(new ArrayList<>());
                }
                video.getVideoCategories().add(vcat);

                videoRepository.save(video);
            }
        }
    }

    // 지역별 조회, etc
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

    // Youtube API + DB fallback 조합 영상 조회
    public List<VideoResponseDTO> searchMixedVideos(String keyword, int apiCount) {
        List<VideoResponseDTO> dbList = searchByKeyword(keyword);
        List<Map<String, Object>> apiList = youtubeApiService.searchVideosByKeyword(keyword, apiCount);

        List<VideoResponseDTO> apiDtoList = new ArrayList<>();
        if (apiList != null && !apiList.isEmpty()) {
            apiDtoList = apiList.stream()
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
        }

        if (apiDtoList.isEmpty()) {
            return dbList;
        }

        Map<String, VideoResponseDTO> mergedMap = new LinkedHashMap<>();
        for (VideoResponseDTO dto : dbList) mergedMap.put(dto.getVideoId(), dto);
        for (VideoResponseDTO dto : apiDtoList) mergedMap.putIfAbsent(dto.getVideoId(), dto);

        return new ArrayList<>(mergedMap.values());
    }
}