package com.minute.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistoryResponseDTO {
    // 검색 기록 조회 응답
    private String userId;
    private String keyword;
    private LocalDateTime searchedAt;
}
