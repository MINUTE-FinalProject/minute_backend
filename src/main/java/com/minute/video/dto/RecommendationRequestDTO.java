package com.minute.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationRequestDTO {
    // 사용자의 추천 요청 시 필요한 정보
    private String userId;
    private String context;   // 이건 좀 더 확인 // 사용자가 추천 받을 상황 (예: 현재 위치, 관심사 등)

}
