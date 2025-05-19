package com.minute.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponseDTO {
    // 추천 결과 반환 응답용
    private String videoId;
    private String videoTitle;
    private String videoUrl;
    private String thumbnailUrl;
    private String categoryName;
    private String channelName;
    private List<String> tagNames;
}
