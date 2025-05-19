package com.minute.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistoryResponseDTO {
    // 사용자가 시청한 영상 목록
    private String videoId;
    private String videoTitle;
    private String videoUrl;
    private String thumbnailUrl;
    private LocalDateTime watchedAt;
}
