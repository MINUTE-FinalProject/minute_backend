package com.minute.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "WatchHistoryRequestDTO",description = "시청 기록 저장 요청 DTO")
public class WatchHistoryRequestDTO {

//    @Schema(description = "사용자 고유 ID", example = "user123")
//    private String userId;
    @Schema(description = "영상 고유 ID", example = "video987")
    private String videoId;
}
