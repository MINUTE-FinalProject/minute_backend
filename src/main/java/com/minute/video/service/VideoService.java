package com.minute.video.service;

import com.minute.video.Entity.Tag;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoCategory;
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
public class VideoService {
    // 영상 조회 및 영상 상세 정보

    private final VideoRepository videoRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final VideoLikesRepository videoLikesRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    // 1. 비로그인 상태 - 전체 영상 조회
    public List<VideoResponseDTO> getAllVideos() {
        return videoRepository.findTop50ByOrderByUploadDateDesc().stream()
                .map(video -> new VideoResponseDTO(
                        video.getVideoId(),
                        video.getVideoTitle(),
                        video.getVideoDescription(),
                        video.getVideoUrl(),
                        video.getThumbnailUrl(),
                        video.getVideoCategories().stream()
                                .map(vc -> vc.getCategory().getCategoryName())
                                .collect(Collectors.toList()),
                        video.getChannel().getChannelName(),
                        video.getVideoTags().stream()
                                .map(vt -> vt.getTag().getTagName())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    // 2. 로그인 상태 - 추천 영상 조회
    public List<VideoResponseDTO> getRecommendedVideos(String userId) {
        // 최근 시청한 영상 목록 가져오기
        List<String> watchedVideoIds = watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(userId).stream()
                .map(history -> history.getVideo().getVideoId())
                .collect(Collectors.toList());

        // 좋아요를 많이 받은 인기 영상 (기본 추천)
        List<Video> popularVideos = videoRepository.findTop50ByOrderByUploadDateDesc();

        // 사용자 검색 기록 기반 추천 (키워드 유사도 기반)
        List<String> keywords = searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId).stream()
                .map(history -> history.getKeyword())
                .collect(Collectors.toList());

        // 사용자 관심 태그 기반 추천
        List<String> favoriteTags = videoLikesRepository.findByUserId(userId).stream()
                .flatMap(videoLikes -> videoLikes.getVideo().getVideoTags().stream())
                .map(videoTag -> videoTag.getTag().getTagName())
                .distinct()
                .collect(Collectors.toList());

        return popularVideos.stream()
                // 1. 시청한 영상 제외
                .filter(video -> !watchedVideoIds.contains(video.getVideoId()))
                // 2. 관심 태그와 일치하는 영상 우선 추천
                .sorted(Comparator.comparing((Video v) ->
                                favoriteTags.stream().anyMatch(tag -> v.getVideoTags().stream()
                                        .anyMatch(videoTag -> videoTag.getTag().getTagName().equals(tag))))
                        .reversed())
                // 3. 검색 기록 키워드와 일치하는 영상 우선 추천
                .sorted(Comparator.comparing((Video v) ->
                                keywords.stream().anyMatch(keyword -> v.getVideoTitle().contains(keyword)))
                        .reversed())
                .map(video -> new VideoResponseDTO(
                        video.getVideoId(),
                        video.getVideoTitle(),
                        video.getVideoDescription(),
                        video.getVideoUrl(),
                        video.getThumbnailUrl(),
                        video.getVideoCategories().stream()
                                .map(vc -> vc.getCategory().getCategoryName())
                                .collect(Collectors.toList()),
                        video.getChannel().getChannelName(),
                        video.getVideoTags().stream()
                                .map(vt -> vt.getTag().getTagName())
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}



