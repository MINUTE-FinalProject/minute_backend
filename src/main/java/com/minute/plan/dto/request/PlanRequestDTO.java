package com.minute.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "일정 생성/수정 요청 DTO")
public class PlanRequestDTO {

    @Schema(description = "여행 날짜", example = "2025-05-26", required = true)
    @NotNull
    private LocalDate travelDate;

    @Schema(description = "제목", example = "박물관 관람", required = true)
    @NotBlank
    private String title;

    @Schema(description = "설명", example = "루브르 박물관 투어")
    private String description;

    @Schema(description = "시작 시간", example = "09:00", required = true)
    @NotNull
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "12:00", required = true)
    @NotNull
    private LocalTime endTime;
}
