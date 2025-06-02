package com.minute.calendar.dto.response;

import com.minute.checklist.dto.response.ChecklistResponseDTO;
import com.minute.plan.dto.response.PlanResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "캘린더 상세 조회 응답 DTO")
public class CalendarResponseDTO {
    @Schema(description = "해당 날짜 일정 목록")
    private List<PlanResponseDTO> plans;

    @Schema(description = "해당 날짜 체크리스트 목록")
    private List<ChecklistResponseDTO> checklists;
}
