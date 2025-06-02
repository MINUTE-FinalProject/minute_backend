package com.minute.video.service;

import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.video.Entity.Category;
import com.minute.video.Entity.Tag;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoCategory;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.mapper.VideoMapper;
import com.minute.video.mapper.VideoResponseMapper;
import com.minute.video.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final VideoResponseMapper videoResponseMapper;
    private final BookmarkRepository bookmarkRepository;
    private final YoutubeApiService youtubeApiService;
    private final CategoryRepository categoryRepository;

    private static final int RECOMMEND_SIZE = 10;

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

    /**
     * 로그인 사용자를 위한 추천 영상 목록
     * 1) 사용자가 본 영상, 좋아요/북마크/태그/검색 기록을 기준으로 점수 계산 → 정렬
     * 2) 정렬된 리스트에서 최대 RECOMMEND_SIZE개 꺼내기
     * 3) 만약 이 목록이 RECOMMEND_SIZE보다 작으면, 부족한 만큼 DB의 다른 영상으로 채워준다
     */
    public List<VideoResponseDTO> getRecommendedVideos(String userId) {
        // 1. 이미 본 영상 ID
        List<String> watchedVideoIds = watchHistoryRepository
                .findByUserUserIdOrderByWatchedAtDesc(userId)
                .stream()
                .map(history -> history.getVideo().getVideoId())
                .toList();

        // 2. 좋아요한 영상 ID
        List<String> likedVideoIds = videoLikesRepository
                .findByUserUserId(userId)
                .stream()
                .map(like -> like.getVideo().getVideoId())
                .distinct()
                .toList();

        // 3. 좋아요 기반 관심 태그 추출
        List<String> favoriteTags = videoLikesRepository
                .findByUserUserId(userId)
                .stream()
                .flatMap(videoLikes -> videoLikes.getVideo().getVideoTags().stream())
                .map(videoTag -> videoTag.getTag().getTagName())
                .distinct()
                .toList();

        // 4. 최근 검색 키워드
        List<String> keywords = searchHistoryRepository
                .findByUserUserIdOrderBySearchedAtDesc(userId)
                .stream()
                .map(searchHistory -> searchHistory.getKeyword())
                .toList();

        // 5. 북마크한 영상 ID
        List<String> bookmarkedVideoIds = bookmarkRepository.findAll().stream()
                .filter(bookmark -> bookmark.getUserId().equals(userId))
                .map(bookmark -> bookmark.getVideoId())
                .distinct()
                .toList();

        // 6. 추천 후보 영상: 좋아요/조회수 상위 50개를 합쳐두고 중복 제거
        List<Video> topByViews = videoRepository.findTop50ByOrderByViewsDesc();
        List<Video> topByLikes = videoRepository.findTop50ByOrderByLikesDesc();
        List<Video> candidates = Stream.concat(topByViews.stream(), topByLikes.stream())
                .distinct()
                .limit(50)
                .collect(Collectors.toList());

        // 7. 점수 계산 후 정렬 → DTO로 변환 전 리스트
        List<Video> scoredList = candidates.stream()
                // 이미 본 영상 제외
                .filter(video -> !watchedVideoIds.contains(video.getVideoId()))
                // 기본 점수 계산
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

        // 8. 점수 순으로 최대 RECOMMEND_SIZE개 가져오기
        List<Video> topRecommended = scoredList.stream()
                .limit(RECOMMEND_SIZE)
                .collect(Collectors.toList());

        // 9. 모자란 개수만큼 다른 영상으로 채워야 하면
        if (topRecommended.size() < RECOMMEND_SIZE) {
            // 이미 추천 목록에 들어간 ID + 이미 본 영상 ID를 Set에 담아 제외
            Set<String> excludeIds = topRecommended.stream()
                    .map(Video::getVideoId)
                    .collect(Collectors.toSet());
            excludeIds.addAll(watchedVideoIds);

            int remaining = RECOMMEND_SIZE - topRecommended.size();

            // 예시: 조회수 상위 50개 중에서 excludeIds에 없는 것들을 채우거나,
            // 아니면 랜덤으로 뽑아도 된다. 여기서는 조회수 상위로 채워보자.
            List<Video> filler = videoRepository.findTop50ByOrderByViewsDesc().stream()
                    .filter(v -> !excludeIds.contains(v.getVideoId()))
                    .limit(remaining)
                    .collect(Collectors.toList());

            topRecommended.addAll(filler);
        }

        // 10. 최종 리스트를 DTO로 변환 후 반환
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
    /**
     * @param videoList   유튜브 API 응답(items 리스트)
     * @param categoryName 원하는 카테고리("캠핑" / "힐링" / "산" / "테마파크")
     */
    @Transactional
    public void saveVideosFromApi(List<Map<String, Object>> videoList, String categoryName) {
        // 1) Category 엔티티 가져오기 (없으면 예외)
        Category category = categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리: " + categoryName));

        for (Map<String, Object> videoMap : videoList) {
            Map<String, Object> idMap      = (Map<String, Object>) videoMap.get("id");
            Map<String, Object> snippetMap = (Map<String, Object>) videoMap.get("snippet");
            if (idMap == null || snippetMap == null) continue;

            String videoId = (String) idMap.get("videoId");
            if (videoId == null) continue;

            // 2) Video 엔티티 생성 또는 조회
            Video video = videoRepository.findById(videoId).orElse(null);
            String title       = (String) snippetMap.get("title");
            String description = (String) snippetMap.get("description");
            String videoUrl    = "https://www.youtube.com/watch?v=" + videoId;

            // 썸네일 추출
            String thumbnailUrl = "";
            Map<String, Object> thumbs = (Map<String,Object>) snippetMap.get("thumbnails");
            if (thumbs != null && thumbs.get("default") != null) {
                thumbnailUrl = (String) ((Map<String,Object>)thumbs.get("default")).get("url");
            }

            if (video == null) {
                // 신규 Video 엔티티
                video = Video.builder()
                        .videoId(videoId)
                        .videoTitle(title)
                        .videoDescription(description)
                        .videoUrl(videoUrl)
                        .thumbnailUrl(thumbnailUrl)
                        // region / city 는 필요 시 채워도 되고 기본값으로 둡니다.
                        .region("")
                        .city("")
                        .build();
            } else {
                // 기존 Video 업데이트 (제목/설명/썸네일만 덮어쓰기)
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
            // Video 테이블에 저장(없으면 Insert, 있으면 Update)
            videoRepository.save(video);

            // 3) VideoCategory 매핑: 이미 연결된 적 없다면 새로 만들기
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

                // 연관관계 편의 메서드가 없을 경우, 직접 추가
                if (video.getVideoCategories() == null) {
                    video.setVideoCategories(new ArrayList<>());
                }
                video.getVideoCategories().add(vcat);

                // VideoRepository.save(video) 를 다시 호출하면
                // cascade = CascadeType.ALL 이므로 VideoCategory도 함께 저장됩니다.
                videoRepository.save(video);
            }
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
