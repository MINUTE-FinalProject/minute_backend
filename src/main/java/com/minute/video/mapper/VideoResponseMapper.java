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
}
