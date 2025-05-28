package com.minute.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Plan 응답 DTO")
public class PlanResponseDTO {
    @Schema(description = "일정 ID", example = "10")
    private Integer planId;

    @Schema(description = "제목", example = "박물관 관람")
    private String title;

    @Schema(description = "설명", example = "루브르 박물관 투어")
    private String description;

    @Schema(description = "시작 시간", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "12:00")
    private LocalTime endTime;
}
