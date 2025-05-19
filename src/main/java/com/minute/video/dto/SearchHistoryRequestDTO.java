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
public class SearchHistoryRequestDTO {
    // 사용자의 검색 기록 저장용
    private String userId;
    private String keyword;
    private LocalDateTime searchedAt;
}
