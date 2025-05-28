package com.minute.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "VideoLikesRequestDTO", description = "좋아요한 영상 저장 요청 DTO")
public class VideoLikesRequestDTO {

    @Schema(description = "사용자 고유 ID", example = "user123")
    private String userId;
    @Schema(description = "영상 고유 ID", example = "video987")
    private String videoId;
}