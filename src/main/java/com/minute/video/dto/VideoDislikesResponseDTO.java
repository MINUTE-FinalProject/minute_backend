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
@Schema(name = "VideoDislikesResponseDTO", description = "싫어요한 영상 목록 조회 응답 DTO")
public class VideoDislikesResponseDTO {
    @Schema(description = "영상 고유 ID", example = "video987")
    private String videoId;

    @Schema(description = "영상 제목", example = "제주도 여행 브이로그")
    private String videoTitle;

    @Schema(description = "유튜브 영상 URL", example = "https://youtu.be/XyZ123Abc")
    private String videoUrl;

    @Schema(description = "썸네일 이미지 URL", example = "https://img.youtube.com/vi/XyZ123Abc/maxresdefault.jpg")
    private String thumbnailUrl;

    @Schema(description = "싫어요한 시각")
    private LocalDateTime createdAt;

}