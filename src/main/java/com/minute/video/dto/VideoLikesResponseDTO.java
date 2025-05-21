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
    // 좋아요한 영상 목록 조회 응답
    private String videoId;
    private String videoTitle;
    private String videoUrl;
    private String thumbnailUrl;
}
