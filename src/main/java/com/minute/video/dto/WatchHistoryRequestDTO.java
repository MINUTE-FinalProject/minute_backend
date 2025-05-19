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
public class WatchHistoryRequestDTO {
    // 사용자의 시청 기록 저장용
    private String userId;
    private String videoId;
    private LocalDateTime watchedAt;
}
