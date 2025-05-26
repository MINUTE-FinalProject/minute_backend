package com.minute.mypage.dto.response;

import com.minute.checklist.dto.response.ChecklistResponseDTO;
import com.minute.plan.dto.response.PlanResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
@Schema(description = "특정 날짜의 Plan·Checklist 응답 DTO")
public class DateDetailResponseDTO {
    @Schema(description = "해당 날짜의 일정 목록")
    private final List<PlanResponseDTO> plans;

    @Schema(description = "해당 날짜의 체크리스트 목록")
    private final List<ChecklistResponseDTO> checklists;
}
