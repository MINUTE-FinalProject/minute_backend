package com.minute.checklist.dto.response;

import com.minute.checklist.entity.Checklist;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Checklist 응답 DTO")
public class ChecklistResponseDTO {
    @Schema(description = "체크리스트 ID", example = "5")
    private Integer checklistId;

    @Schema(description = "연관된 일정 ID", example = "10")
    private Integer planId;

    @Schema(description = "내용", example = "여권 챙기기")
    private String itemContent;

    @Schema(description = "체크 여부", example = "false")
    private Boolean isChecked;

    // 엔티티 DTO 변환 헬퍼 메서드
    public static ChecklistResponseDTO fromEntity(Checklist c) {
        Integer planId = null;
        if (c.getPlan() != null) {
            planId = c.getPlan().getPlanId();
        }
        return ChecklistResponseDTO.builder()
                .checklistId(c.getChecklistId())
                .planId(planId)
                .itemContent(c.getItemContent())
                .isChecked(c.getIsChecked())
                .build();
    }
}
