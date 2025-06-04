package com.minute.video.mapper;

import com.minute.video.Entity.Video;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.repository.VideoLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoResponseMapper {
    private final VideoMapper videoMapper;
    private final VideoLikesRepository videoLikesRepository;

    public VideoResponseDTO toDtoWithStats(Video video) {
        VideoResponseDTO dto = videoMapper.toDto(video);
        dto.setLikes(videoLikesRepository.countByVideoVideoId(video.getVideoId()));
        dto.setViews(video.getViews());
        return dto;
    }

    // 추천 점수를 함께 전달받는 오버로드 메서드
    public VideoResponseDTO toDtoWithStats(Video video, int score) {
        VideoResponseDTO dto = toDtoWithStats(video); // 위 메서드 재사용
        dto.setRecommendationScore(score);            // 추천 점수 추가
        return dto;
    }
}
