package com.minute.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLikesRequestDTO {
    // 사용자의 좋아요 기록 저장용
    private String userId;
    private String videoId;
}
