package com.minute.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "VideoResponseDTO", description = "영상 조회 응답 DTO")
public class VideoResponseDTO {
    // 추천 결과나 목록 보여줄 때 필요 응답용

    @Schema(description = "영상 고유 ID", example = "xyz123abs")
    private String videoId;
    @Schema(description = "영상 제목", example = "부산 여행")
    private String videoTitle;
    @Schema(description = "영상 설명", example = "부산 여행을 시작합니다.." )
    private String videoDescription;
    @Schema(description = "유튜브 영상 URL", example = "https://youtu.be/XyZ123Abc")
    private String videoUrl;
    @Schema(description = "썸네일 이미지 URL", example = "https://img.youtube.com/vi/XyZ123Abc/maxresdefault.jp")
    private String thumbnailUrl;
    @Schema(description = "카테고리 리스트", example = "[\"여행\", \"캠핑\"]")
    private List<String> categoryNames;
    @Schema(description = "채널이름")
    private String channelName;
    @Schema(description = "태그리스트",example = "[\"부산\", \"서울\"]")
    private List<String> tagNames;
    @Schema(description = "조회수 수")
    private Long views;
    @Schema(description = "좋아요 수")
    private Long likes;

    // 추가: 추천 점수
    private Integer recommendationScore;

}
