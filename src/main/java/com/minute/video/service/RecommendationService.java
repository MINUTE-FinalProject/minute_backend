package com.minute.video.service;

import com.minute.video.Entity.Video;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.repository.SearchHistoryRepository;
import com.minute.video.repository.VideoLikesRepository;
import com.minute.video.repository.VideoRepository;
import com.minute.video.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    // 사용자별 추천 알고리즘

    private final VideoRepository videoRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final VideoLikesRepository videoLikesRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    // 추천 목록 가져오기 (좋아요, 북마크,검색, 시청기록)
    // 사용자의 추천 영상 목록 조회
    public List<VideoResponseDTO> getRecommendedVideos(String userId) {
        // 1. 최근 시청한 영상 기반 추천 (최근 시청 영상과 유사한 영상 추천)
        List<String> watchedVideoIds = watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(userId).stream()
                .map(watchHistory -> watchHistory.getVideo().getVideoId())
                .collect(Collectors.toList());

        // 2. 좋아요를 많이 받은 인기 영상 (기본 추천)
        List<Video> popularVideos = videoRepository.findTop10ByOrderByLikesDesc();

        // 3. 사용자 검색 기록 기반 추천 (키워드 유사도 기반)
        List<String> keywords = searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId).stream()
                .map(searchHistory -> searchHistory.getKeyword())
                .collect(Collectors.toList());

        // 4. 사용자 관심사 기반 필터링 (태그 기반)
        List<String> favoriteTags = videoLikesRepository.findByUserId(userId).stream()
                .flatMap(videoLikes -> videoLikes.getVideo().getVideoTags().stream())
                .map(videoTag -> videoTag.getTag().getTagName())
                .distinct()
                .collect(Collectors.toList());

        return popularVideos.stream()
                // 5. 필터링: 이미 시청한 영상 제외
                .filter(video -> !watchedVideoIds.contains(video.getVideoId()))
                // 6. 필터링: 관심 태그와 일치하는 영상 우선 추천
                .sorted(Comparator.comparing((Video v) ->
                                favoriteTags.stream().anyMatch(tag -> v.getVideoTags().stream()
                                        .anyMatch(videoTag -> videoTag.getTag().getTagName().equals(tag))))
                        .reversed())
                // 7. 필터링: 검색 기록 키워드와 일치하는 영상 우선 추천
                .sorted(Comparator.comparing((Video v) ->
                                keywords.stream().anyMatch(keyword -> v.getVideoTitle().contains(keyword)))
                        .reversed())
                .map(video -> {
                    // 태그 목록 추출
                    List<String> tagNames = video.getVideoTags().stream()
                            .map(videoTag -> videoTag.getTag().getTagName())
                            .collect(Collectors.toList());

                    // 카테고리 목록 추출
                    List<String> categoryNames = video.getVideoCategories().stream()
                            .map(videoCategory -> videoCategory.getCategory().getCategoryName())
                            .collect(Collectors.toList());

                    return new VideoResponseDTO(
                            video.getVideoId(),
                            video.getVideoTitle(),
                            video.getVideoDescription(),
                            video.getVideoUrl(),
                            video.getThumbnailUrl(),
                            categoryNames,
                            video.getChannel().getChannelName(),
                            tagNames
                    );
                })
                .collect(Collectors.toList());
    }

}
