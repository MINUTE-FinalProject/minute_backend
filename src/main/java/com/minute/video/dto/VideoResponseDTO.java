package com.minute.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponseDTO {
    // 추천 결과나 목록 보여줄 때 필요 응답용
    private String videoId;
    private String videoTitle;
    private String videoDescription;
    private String videoUrl;
    private String thumbnailUrl;

    private List<String> categoryNames;
    private String channelName;
    private List<String> tagNames;


}
