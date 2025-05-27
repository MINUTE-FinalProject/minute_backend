package com.minute.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 응답 시 사용될 예시 DTO")
public class ExampleResponseDto {

    @Schema(description = "처리 결과 ID", example = "100")
    private Long resultId;

    @Schema(description = "처리 상태 메시지", example = "성공적으로 처리되었습니다.")
    private String message;

    @Schema(description = "요청된 항목 이름", example = "샘플 항목")
    private String requestedItemName;

    // --- 생성자 및 Getter/Setter ---
    public ExampleResponseDto(Long resultId, String message, String requestedItemName) {
        this.resultId = resultId;
        this.message = message;
        this.requestedItemName = requestedItemName;
    }

    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRequestedItemName() { return requestedItemName; }
    public void setRequestedItemName(String requestedItemName) { this.requestedItemName = requestedItemName; }
}