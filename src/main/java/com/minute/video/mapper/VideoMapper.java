package com.minute.video.mapper;

import com.minute.video.Entity.Video;
import com.minute.video.dto.VideoResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VideoMapper {
    // videoResponseDTO 반환  공통
    public VideoResponseDTO toDto(Video video){
        List<String> categoryNames  = video.getVideoCategories().stream()
                .map(videoCategory -> videoCategory.getCategory().getCategoryName())
                .collect(Collectors.toList());

        List<String> tagNames = video.getVideoTags().stream()
                .map(videoTag -> videoTag.getTag().getTagName())
                .collect(Collectors.toList());

        return VideoResponseDTO.builder()
                .videoId(video.getVideoId())
                .videoTitle(video.getVideoTitle())
                .videoDescription(video.getVideoDescription())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .categoryNames(categoryNames)
                .channelName(video.getChannel().getChannelName())
                .tagNames(tagNames)
                .views(video.getViews())
                .likes(video.getLikes())
                .build();
    }
}
