package com.minute.checklist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "체크리스트 생성/수정 요청 DTO")
public class ChecklistRequestDTO {

    @Schema(description = "여행 날짜", example = "2025-05-26", required = true)
    @NotNull
    private LocalDate travelDate;

    @Schema(description = "연관된 Plan ID", example = "10", required = true)
    private Integer planId;

    @Schema(description = "체크리스트 항목 내용", example = "카메라 준비", required = true)
    @NotBlank
    private String itemContent;

    @Schema(description = "체크 상태 (true: 완료, false: 미완료)", example = "false", required = true)
    @NotNull
    private Boolean isChecked;
}
