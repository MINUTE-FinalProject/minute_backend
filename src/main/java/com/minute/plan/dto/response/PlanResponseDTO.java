package com.minute.plan.dto.response;

import com.minute.plan.entity.Plan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Plan 응답 DTO")
public class PlanResponseDTO {
    @Schema(description = "일정 ID", example = "10")
    private Integer planId;

    @Schema(description = "여행 날짜", example = "2025-05-26")
    private LocalDate travelDate;

    @Schema(description = "제목", example = "박물관 관람")
    private String title;

    @Schema(description = "설명", example = "루브르 박물관 투어")
    private String description;

    @Schema(description = "시작 시간", example = "09:00")
    private LocalTime startTime;

    @Schema(description = "종료 시간", example = "12:00")
    private LocalTime endTime;

    // 엔티티 DTO 변환 헬퍼 메서드
    public static PlanResponseDTO fromEntity(Plan p) {
        return PlanResponseDTO.builder()
                .planId(p.getPlanId())
                .travelDate(p.getTravelDate())
                .title(p.getTitle())
                .description(p.getDescription())
                .startTime(p.getStartTime())
                .endTime(p.getEndTime())
                .build();
    }
}
