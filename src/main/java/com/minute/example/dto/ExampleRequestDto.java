package com.minute.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 요청 시 사용될 예시 DTO")
public class ExampleRequestDto {

    @Schema(description = "사용자 ID (필수)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "항목 이름 (필수, 최소 2자, 최대 50자)", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50, example = "샘플 항목")
    private String itemName;

    @Schema(description = "항목 설명 (선택)", example = "이것은 샘플 항목에 대한 설명입니다.")
    private String description;

    // --- 실제 로직은 주석 처리하거나 최소화 ---
    // Getter/Setter는 DTO의 기본입니다.
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}