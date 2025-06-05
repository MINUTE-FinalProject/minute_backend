package com.minute.video.service;

import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.video.Entity.*;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.mapper.VideoResponseMapper;
import com.minute.video.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {
    // ─────────────────────────────────────────────────────────────────────────
    // 의존성 주입
    // ─────────────────────────────────────────────────────────────────────────
    private final VideoRepository videoRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final VideoLikesRepository videoLikesRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final VideoResponseMapper videoResponseMapper;
    private final BookmarkRepository bookmarkRepository;
    private final YoutubeApiService youtubeApiService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final VideoFilterService videoFilterService;

    private static final int RECOMMEND_SIZE = 30;


    // ─────────────────────────────────────────────────────────────────────────
    // 1) 필터링 로직: “여행 관련 여부” 및 “카테고리별 캠핑 전용 여부”
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 제목/설명 중에 “여행” 또는 “가볼만” 등의 키워드가 하나라도 있으면
     * 여행 관련 콘텐츠로 판단해서 true를 리턴합니다.
     */
    private boolean isTravelRelated(Video video) {
        String title = video.getVideoTitle() != null ? video.getVideoTitle() : "";
        String desc  = video.getVideoDescription() != null ? video.getVideoDescription() : "";

        // 허용 키워드 리스트 (원하시는 키워드를 추가/수정하세요)
        List<String> allowed = List.of("여행", "가볼만", "트레킹", "관광", "바캉스", "맛집", "투어");

        // 제목이나 설명에 allowed 리스트 중 하나라도 포함되어 있으면 true
        for (String kw : allowed) {
            if (title.contains(kw) || desc.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 1) 비로그인: 전체 영상 조회
     * 2) 로그인: 추천 영상 조회
     */
    public List<VideoResponseDTO> getVideos(String userId) {
        if (userId == null || userId.isBlank()) {
            return getAllVideos();
        } else {
            return getRecommendedVideos(userId);
        }
    }

    /** 전체 영상 조회 (최신순 50개) */
    public List<VideoResponseDTO> getAllVideos() {
        return videoRepository
                .findTop50ByOrderByVideoIdDesc()
                .stream()
                // ← 이 부분에서 isTravelRelated 체크
                .filter(video -> isTravelRelated(video))
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    /**
     * 로그인 사용자를 위한 추천 영상 목록
     * 1) 이미 본 영상, 좋아요/북마크/검색 이력 기반 점수 계산
     *    + 시청 카테고리 기반 보정 (+1점)
     * 2) 상위 RECOMMEND_SIZE개 선택
     * 3) 부족 시 조회수 상위로 채움
     */
    public List<VideoResponseDTO> getRecommendedVideos(String userId) {
        // 1) 이미 본 영상 ID 목록
        List<String> watchedVideoIds = watchHistoryRepository
                .findByUserUserIdOrderByWatchedAtDesc(userId)
                .stream()
                .map(history -> history.getVideo().getVideoId())
                .toList();

        // 2) 좋아요한 영상 ID 목록
        List<String> likedVideoIds = videoLikesRepository
                .findByUserUserId(userId)
                .stream()
                .map(like -> like.getVideo().getVideoId())
                .distinct()
                .toList();

        // 3) 최근 검색 키워드 목록
        List<String> keywords = searchHistoryRepository
                .findByUserUserIdOrderBySearchedAtDesc(userId)
                .stream()
                .map(SearchHistory::getKeyword)
                .toList();

        // 4) 북마크한 영상 ID 목록
        List<String> bookmarkedVideoIds = bookmarkRepository.findAll().stream()
                .filter(bookmark -> bookmark.getUserId().equals(userId))
                .map(Bookmark::getVideoId)
                .distinct()
                .toList();

        // 0) 시청 이력으로 “카테고리별 시청 횟수” 집계
        Map<String, Integer> watchedCategoryCount = buildWatchedCategoryCount(userId);

        // 5) 추천 후보 영상: 조회수 상위 50, 좋아요 상위 50 중복 제거 후 최대 200개
        List<Video> topByViews = videoRepository.findTop50ByOrderByViewsDesc();
        List<Video> topByLikes = videoRepository.findTop50ByOrderByLikesDesc();
        List<Video> candidates = Stream.concat(topByViews.stream(), topByLikes.stream())
                .distinct()
                .limit(200)
                .collect(Collectors.toList());

        // 6) 점수 계산 및 로그
        Map<String, Integer> scoreMap = new HashMap<>();
        for (Video video : candidates) {
            if (watchedVideoIds.contains(video.getVideoId())) continue;
            int score = calculateScore(
                    video,
                    likedVideoIds,
                    bookmarkedVideoIds,
                    keywords,
                    watchedCategoryCount   // ← 카테고리 보정 맵 전달
            );
            scoreMap.put(video.getVideoId(), score);
            log.info("[추천점수] {} ({}) → {}점",
                    video.getVideoTitle(), video.getVideoId(), score);
        }

        // 7) 점수 기준으로 정렬
        List<Video> scoredList = candidates.stream()
                .filter(video -> !watchedVideoIds.contains(video.getVideoId()))
                .sorted(Comparator.comparingInt((Video video) ->
                        calculateScore(
                                video,
                                likedVideoIds,
                                bookmarkedVideoIds,
                                keywords,
                                watchedCategoryCount
                        )
                ).reversed())
                .collect(Collectors.toList());

        // 8) 상위 RECOMMEND_SIZE개 선택
        List<Video> topRecommended = scoredList.stream()
                .limit(RECOMMEND_SIZE)
                .collect(Collectors.toList());

        // 9) 부족 시 조회수 상위로 채워넣기
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

        // 10) DTO로 변환하여 반환 (점수 포함)
        return topRecommended.stream()
                .map(video -> {
                    int score = scoreMap.getOrDefault(video.getVideoId(), 0);
                    return videoResponseMapper.toDtoWithStats(video, score);
                })
                .collect(Collectors.toList());
    }

    /** 개별 영상 점수 계산 (태그 매칭 제거, 대신 시청 카테고리 기반 +1점) */
    private int calculateScore(
            Video video,
            List<String> likedVideoIds,
            List<String> bookmarkedVideoIds,
            List<String> keywords,
            Map<String, Integer> watchedCategoryCount   // ← 추가된 파라미터
    ) {
        int score = 0;

        // 좋아요한 영상 +5
        if (likedVideoIds.contains(video.getVideoId())) {
            score += 5;
        }

        // 북마크한 영상 +4
        if (bookmarkedVideoIds.contains(video.getVideoId())) {
            score += 4;
        }

        // 제목에 검색 키워드 포함 +2
        if (keywords != null) {
            String lowerTitle = video.getVideoTitle().toLowerCase();
            for (String kw : keywords) {
                if (lowerTitle.contains(kw.toLowerCase())) {
                    score += 2;
                    break;
                }
            }
        }

        // ── 새로 추가: 시청 카테고리 기반 +1점 ──
        if (video.getVideoCategories() != null) {
            for (VideoCategory vc : video.getVideoCategories()) {
                String catName = vc.getCategory().getCategoryName();
                if (watchedCategoryCount.getOrDefault(catName, 0) > 0) {
                    score += 1;
                    break;  // 한 카테고리만 매칭되어도 +1점
                }
            }
        }

        return score;
    }

    /**
     * 사용자가 시청한 영상들의 카테고리를 집계해서,
     * 카테고리명 → 시청 횟수 맵을 리턴
     */
    private Map<String, Integer> buildWatchedCategoryCount(String userId) {
        // 1) 시청 이력에서 videoId 목록 조회
        List<String> watchedVideoIds = watchHistoryRepository
                .findByUserUserIdOrderByWatchedAtDesc(userId)
                .stream()
                .map(history -> history.getVideo().getVideoId())
                .toList();

        if (watchedVideoIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2) Video 엔티티를 한 번에 조회
        List<Video> watchedVideos = videoRepository.findAllById(watchedVideoIds);

        // 3) 카테고리별 집계
        Map<String, Integer> categoryCount = new HashMap<>();
        for (Video v : watchedVideos) {
            if (v.getVideoCategories() == null) continue;
            for (VideoCategory vc : v.getVideoCategories()) {
                String catName = vc.getCategory().getCategoryName();
                categoryCount.put(catName, categoryCount.getOrDefault(catName, 0) + 1);
            }
        }
        return categoryCount;
    }

    /** 카테고리별 영상 조회 */
    public List<VideoResponseDTO> getVideoByCategory(String categoryName) {
        List<Video> all = videoRepository.findByCategoryName(categoryName);

        return all.stream()
                .filter(video -> videoFilterService.isAllowed(video, categoryName))
                .filter(this::isTravelRelated)
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    /** 태그별 영상 조회 (태그 기능이 남아있다면 유지, 아니라면 삭제) */
    public List<VideoResponseDTO> getVideosByTag(String tagName) {
        return videoRepository.findByTagName(tagName).stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    /** 키워드 검색 (제목 기준) */
    public List<VideoResponseDTO> searchByKeyword(String keyword) {
        return videoRepository.findByVideoTitleContainingIgnoreCase(keyword)
                .stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    /**
     * 영상 상세 조회(조회수 증가 + watch history 저장)
     */
    @Transactional
    public VideoResponseDTO getVideoDetailAndIncrement(String videoId, String userId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new EntityNotFoundException("video not found: " + videoId));

        // 조회수 증가
        Long oldViews = video.getViews();
        long newViews = (oldViews == null ? 0L : oldViews) + 1;
        video.setViews(newViews);
        videoRepository.save(video);

        // WatchHistory 저장 (로그인 사용자만)
        if (userId != null && !userId.isBlank()) {
            userRepository.findUserByUserId(userId).ifPresent(user -> {
                WatchHistory history = WatchHistory.builder()
                        .user(user)
                        .video(video)
                        .watchedAt(LocalDateTime.now())
                        .build();
                watchHistoryRepository.save(history);
            });
        }

        return videoResponseMapper.toDtoWithStats(video);
    }

    /** 영상 상세 조회(조회수 증가 없이) */
    public VideoResponseDTO getVideoDetail(String videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new EntityNotFoundException("video not found: " + videoId));
        return videoResponseMapper.toDtoWithStats(video);
    }

    /** 좋아요 기준 인기 영상 조회 (fallback: 조회수, 최신순) */
    public List<VideoResponseDTO> getPopularByLikeCount() {
        List<Video> videos = videoRepository.findTop50ByOrderByLikesDesc();
        if (videos.isEmpty()) {
            videos = videoRepository.findTop50ByOrderByViewsDesc();
        }
        if (videos.isEmpty()) {
            videos = videoRepository.findTop50ByOrderByVideoIdDesc();
        }
        return videos.stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    /** 조회수 기준 인기 영상 조회 (fallback: 최신순) */
    public List<VideoResponseDTO> getPopularByWatchCount() {
        List<Video> videos = watchHistoryRepository.findMostWatchedVideos();
        if (videos.isEmpty()) {
            videos = videoRepository.findTop50ByOrderByViewsDesc();
        }
        if (videos.isEmpty()) {
            videos = videoRepository.findTop50ByOrderByVideoIdDesc();
        }
        return videos.stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }



    /**────────────────────────────────────────────────────────────────────────
     *  Youtube API로부터 받은 영상을 DB에 저장하거나, 수정
     *  (태그 기능 제거하였으므로, 단순히 Video만 저장/수정)
     *
     *  @param videoList    YouTube API 응답 items 리스트
     *  @param categoryName 카테고리 이름(예: "캠핑", "힐링" 등)
     *────────────────────────────────────────────────────────────────────────*/
    @Transactional
    public void saveVideosFromApi(List<Map<String, Object>> videoList, String categoryName) {
        Category category = categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리: " + categoryName));

        for (Map<String, Object> videoMap : videoList) {
            Map<String, Object> idMap = (Map<String, Object>) videoMap.get("id");
            Map<String, Object> snippetMap = (Map<String, Object>) videoMap.get("snippet");
            if (idMap == null || snippetMap == null) continue;

            String videoId = (String) idMap.get("videoId");
            if (videoId == null) continue;

            String title = (String) snippetMap.get("title");
            String description = (String) snippetMap.get("description");
            String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

            String thumbnailUrl = "";
            Map<String, Object> thumbs = (Map<String, Object>) snippetMap.get("thumbnails");
            if (thumbs != null && thumbs.get("default") != null) {
                thumbnailUrl = (String) ((Map<String, Object>) thumbs.get("default")).get("url");
            }

            // 1) Video 엔티티 조회 후 수정 or 신규 생성
            Video video = videoRepository.findById(videoId).orElse(null);
            if (video == null) {
                // 신규 생성
                video = Video.builder()
                        .videoId(videoId)
                        .videoTitle(title)
                        .videoDescription(description)
                        .videoUrl(videoUrl)
                        .thumbnailUrl(thumbnailUrl)
                        .region("")   // 필요 시 region, city를 파라미터로 받아서 설정
                        .city("")
                        .build();
                videoRepository.save(video);
            } else {
                // 기존 영속 객체를 setter로 수정
                video.setVideoTitle(title);
                video.setVideoDescription(description);
                video.setVideoUrl(videoUrl);
                video.setThumbnailUrl(thumbnailUrl);
                // region, city, views, likes 등은 필요 시 setter로 갱신
                videoRepository.save(video);
            }

            // 2) 카테고리 매핑 (이미 매핑된 것이 없으면 새로 추가)
            VideoCategory.VideoCategoryId vcId =
                    new VideoCategory.VideoCategoryId(videoId, category.getCategoryId());
            boolean linkExists = video.getVideoCategories() != null &&
                    video.getVideoCategories().stream()
                            .anyMatch(vcat -> vcat.getId().equals(vcId));
            if (!linkExists) {
                VideoCategory vcat = VideoCategory.builder()
                        .id(vcId)
                        .video(video)
                        .category(category)
                        .build();
                video.getVideoCategories().add(vcat);
                videoRepository.save(video);
            }
        }
    }

    /** 필요 시: 지역별 조회, etc. */
    /** region만 있는 경우, limit개수만큼 조회 */
    public List<Video> getVideosByRegion(String region, int limit) {
        return videoRepository.findByRegion(region, PageRequest.of(0, limit));
    }

    /** region + city가 있는 경우, limit개수만큼 조회 */
    public List<Video> getVideosByRegionAndCity(String region, String city, int limit) {
        return videoRepository.findByRegionAndCity(region, city, PageRequest.of(0, limit));
    }

    /** 아무 파라미터도 없을 때, 전체 영상 중 limit개 조회 */
    public List<Video> getAllVideos(int limit) {
        return videoRepository.findAll(PageRequest.of(0, limit)).getContent();
    }

    public List<VideoResponseDTO> searchByTitleOrRegionOrCity(String keyword) {
        return videoRepository.searchByTitleOrRegionOrCity(keyword).stream()
                .map(videoResponseMapper::toDtoWithStats)
                .collect(Collectors.toList());
    }

    public List<VideoResponseDTO> searchMixedVideos(String keyword, int apiCount) {
        // 1) DB에서 제목 검색
        List<VideoResponseDTO> dbList = searchByKeyword(keyword);

        // 2) YouTube API에서 검색
        List<Map<String, Object>> apiList = youtubeApiService.searchVideosByKeyword(keyword, apiCount);

        // 2-a) “여행과 관련 없는” 영상 걸러내기
        List<Map<String, Object>> filteredApiList = apiList.stream()
                .filter(map -> {
                    Map<String, Object> snippet = (Map<String, Object>) map.get("snippet");
                    if (snippet == null) return false;

                    String title = (String) snippet.get("title");
                    String description = (String) snippet.get("description");
                    String channelTitle = (String) snippet.get("channelTitle");

                    // 예시: 제목/설명에 “여행” 또는 “가볼만” 키워드가 없으면 제외
                    boolean hasTravelKeyword = false;
                    if (title != null && (title.contains("여행") || title.contains("가볼만"))) {
                        hasTravelKeyword = true;
                    } else if (description != null && (description.contains("여행") || description.contains("가볼만"))) {
                        hasTravelKeyword = true;
                    }
                    if (!hasTravelKeyword) {
                        return false;
                    }

                    // 채널명에 “광고”나 “ads”가 들어가 있으면 제외
                    if (channelTitle != null) {
                        String lower = channelTitle.toLowerCase();
                        if (lower.contains("광고") || lower.contains("ads")) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 2-b) DTO 변환
        List<VideoResponseDTO> apiDtoList = filteredApiList.stream()
                .map(apiMap -> {
                    Map<String, Object> idMap = (Map<String, Object>) apiMap.get("id");
                    Map<String, Object> snippet = (Map<String, Object>) apiMap.get("snippet");
                    if (idMap == null || snippet == null) return null;

                    String vid = (String) idMap.get("videoId");
                    String t = (String) snippet.get("title");
                    String d = (String) snippet.get("description");
                    String ch = (String) snippet.get("channelTitle");
                    String url = (vid != null) ? "https://www.youtube.com/watch?v=" + vid : null;

                    String thumb = "";
                    if (snippet.get("thumbnails") != null) {
                        Map<String, Object> thumbs = (Map<String, Object>) snippet.get("thumbnails");
                        if (thumbs.get("default") != null) {
                            thumb = (String) ((Map<String, Object>) thumbs.get("default")).get("url");
                        }
                    }

                    return VideoResponseDTO.builder()
                            .videoId(vid)
                            .videoTitle(t)
                            .videoDescription(d)
                            .videoUrl(url)
                            .thumbnailUrl(thumb)
                            .channelName(ch)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 3) 두 리스트 병합하면서 중복 제거
        Map<String, VideoResponseDTO> merged = new LinkedHashMap<>();
        for (VideoResponseDTO dto : dbList) {
            merged.put(dto.getVideoId(), dto);
        }
        for (VideoResponseDTO dto : apiDtoList) {
            merged.putIfAbsent(dto.getVideoId(), dto);
        }
        return new ArrayList<>(merged.values());
    }

    /**
     * Region, City 단위로 YouTube Shorts 결과를 받아와서
     * Video.region, Video.city 칼럼에 세팅한 뒤 저장하는 메서드
     *
     * @param videoList  YouTube API에서 받아온 item(Map) 리스트
     * @param region     ex) "경기도", "강원도" 등
     * @param city       ex) "가평", "강릉" 등
     */
    @Transactional
    public void saveVideosByRegionAndCity(List<Map<String, Object>> videoList, String region, String city) {
        for (Map<String, Object> videoMap : videoList) {
            Map<String, Object> snippet = (Map<String, Object>) videoMap.get("snippet");
            if (snippet == null) {
                continue;
            }

            String title       = (String) snippet.get("title");
            String description = (String) snippet.get("description");
            String channelName = (String) snippet.get("channelTitle");

            // 예시: 제목/설명에 “여행” 또는 “가볼만” 키워드가 포함되지 않으면 건너뛰기
            if (title == null || !(title.contains("여행") || title.contains("가볼만"))) {
                continue;
            }
            if (channelName != null) {
                String lower = channelName.toLowerCase();
                if (lower.contains("광고") || lower.contains("ads")) {
                    continue;
                }
            }
            if (title.length() < 5 && (description == null || description.length() < 10)) {
                continue;
            }

            Map<String, Object> idMap = (Map<String, Object>) videoMap.get("id");
            if (idMap == null) {
                continue;
            }
            String videoId = (String) idMap.get("videoId");
            if (videoId == null) {
                continue;
            }

            String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

            // 썸네일 URL 추출
            String thumbnailUrl = "";
            Map<String, Object> thumbs = (Map<String, Object>) snippet.get("thumbnails");
            if (thumbs != null && thumbs.get("default") != null) {
                thumbnailUrl = (String) ((Map<String, Object>) thumbs.get("default")).get("url");
            }

            // ── 1) 기존 Video 조회 또는 신규 생성
            Video video = videoRepository.findById(videoId).orElse(null);
            if (video == null) {
                video = Video.builder()
                        .videoId(videoId)
                        .videoTitle(title)
                        .videoDescription(description)
                        .videoUrl(videoUrl)
                        .thumbnailUrl(thumbnailUrl)
                        .region(region)
                        .city(city)
                        .build();
                videoRepository.save(video);
            } else {
                // 기존에 있던 영상이면 region, city, 제목, 설명, 썸네일 등만 업데이트
                video.setVideoTitle(title);
                video.setVideoDescription(description);
                video.setVideoUrl(videoUrl);
                video.setThumbnailUrl(thumbnailUrl);
                video.setRegion(region);
                video.setCity(city);
                videoRepository.save(video);
            }
        }
    }
}
