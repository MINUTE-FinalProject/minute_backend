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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
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

}



