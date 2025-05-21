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
    // 시청 기록 조회 응답
    private String videoId;
    private String videoTitle;
    private String videoUrl;
    private String thumbnailUrl;
    private LocalDateTime watchedAt;
}
