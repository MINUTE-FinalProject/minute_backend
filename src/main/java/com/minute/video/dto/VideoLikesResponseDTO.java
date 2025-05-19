package com.minute.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLikesResponseDTO {
    // 사용자가 좋아요를 누른 영상 목록
    private String videoId;
    private String videoTitle;
    private String videoUrl;
    private String thumbnailUrl;
}
