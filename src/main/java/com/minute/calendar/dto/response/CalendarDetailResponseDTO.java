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
public class CalendarDetailResponseDTO {
    private List<PlanResponseDTO> plans;
    private List<ChecklistResponseDTO> checklists;
}
