package com.minute.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "WatchHistoryResponseDTO",description = "시청 기록 조회 응답 DTO")
public class WatchHistoryResponseDTO {

    @Schema(description = "영상 고유 ID", example = "video987")
    private String videoId;
    @Schema(description = "영상 제목", example = "제주도 여행 브이로그")
    private String videoTitle;
    @Schema(description = "유튜브 영상 URL", example = "https://youtu.be/XyZ123Abc")
    private String videoUrl;
    @Schema(description = "썸네일 이미지 URL", example = "https://img.youtube.com/vi/XyZ123Abc/maxresdefault.jp")
    private String thumbnailUrl;
    @Schema(description = "시청 시간", example = "2025-05-21T14:30:00Z")
    private LocalDateTime watchedAt;
}
