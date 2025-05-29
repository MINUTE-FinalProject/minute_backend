package com.minute.video.mapper;

import com.minute.video.Entity.Video;
import com.minute.video.dto.VideoResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VideoMapper {

    // 영상 엔티티를 DTO로 변환
    public VideoResponseDTO toDto(Video video) {
        List<String> categoryNames = video.getVideoCategories().stream()
                .map(vc -> vc.getCategory().getCategoryName())
                .collect(Collectors.toList());

        List<String> tagNames = video.getVideoTags().stream()
                .map(vt -> vt.getTag().getTagName())
                .collect(Collectors.toList());

        String channelName = (video.getChannel() != null)
                ? video.getChannel().getChannelName()
                : "알 수 없음"; // 또는 null 반환도 가능

        return VideoResponseDTO.builder()
                .videoId(video.getVideoId())
                .videoTitle(video.getVideoTitle())
                .videoDescription(video.getVideoDescription())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .categoryNames(categoryNames)
                .channelName(channelName)
                .tagNames(tagNames)
                .build();
    }
}
