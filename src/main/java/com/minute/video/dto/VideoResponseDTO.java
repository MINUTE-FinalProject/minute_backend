package com.minute.video.dto;

// Channel, Category, Tag, VideoCategory, VideoTag 엔티티는 Video 엔티티 내에서 getter를 통해 접근하므로,
// 이 DTO 파일에서 직접 임포트할 필요는 없습니다.
// 단, Video 엔티티 및 연관 엔티티들에 해당 정보를 가져올 수 있는 getter 메소드가 있어야 합니다.
import com.minute.video.Entity.Video;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    @Schema(description = "영상 설명", example = "부산 여행을 시작합니다..")
    private String videoDescription;
    @Schema(description = "유튜브 영상 URL", example = "https://youtu.be/XyZ123Abc")
    private String videoUrl;
    @Schema(description = "썸네일 이미지 URL", example = "https://img.youtube.com/vi/XyZ123Abc/maxresdefault.jp")
    private String thumbnailUrl;
    @Schema(description = "지역", example = "부산")
    private String region;
    @Schema(description = "도시", example = "해운대구")
    private String city;
    @Schema(description = "채널이름")
    private String channelName; // Channel 엔티티의 이름
    @Schema(description = "카테고리 리스트", example = "[\"여행\", \"캠핑\"]")
    private List<String> categoryNames;
    @Schema(description = "태그리스트", example = "[\"부산\", \"서울\"]")
    private List<String> tagNames;
    @Schema(description = "조회수 수")
    private Long views;
    @Schema(description = "좋아요 수")
    private Long likes;
    // 추가: 추천 점수
    private Integer recommendationScore;
    /**
     * Video 엔티티를 VideoResponseDTO로 변환하는 정적 팩토리 메소드.
     * 제공해주신 Video 엔티티의 구조를 기반으로 작성되었습니다.
     *
     * @param video 변환할 Video 엔티티
     * @return 변환된 VideoResponseDTO
     */
    public static VideoResponseDTO fromEntity(Video video) {
        if (video == null) {
            return null;
        }

        List<String> categoryNamesList;
        if (video.getVideoCategories() != null && !video.getVideoCategories().isEmpty()) {
            categoryNamesList = video.getVideoCategories().stream()
                    .filter(vc -> vc != null && vc.getCategory() != null && vc.getCategory().getCategoryName() != null)
                    .map(vc -> vc.getCategory().getCategoryName()) // VideoCategory -> Category -> CategoryName
                    .collect(Collectors.toList());
        } else {
            categoryNamesList = Collections.emptyList();
        }

        List<String> tagNamesList;
        if (video.getVideoTags() != null && !video.getVideoTags().isEmpty()) {
            tagNamesList = video.getVideoTags().stream()
                    .filter(vt -> vt != null && vt.getTag() != null && vt.getTag().getTagName() != null)
                    .map(vt -> vt.getTag().getTagName()) // VideoTag -> Tag -> TagName
                    .collect(Collectors.toList());
        } else {
            tagNamesList = Collections.emptyList();
        }

        String channelNameStr = null;
        // Channel 엔티티에 채널명을 가져오는 getter (예: getChannelName())가 있다고 가정합니다.
        // Video 엔티티에 Channel channel; 필드와 getChannel() 메소드가 있어야 합니다.
        // Channel 엔티티에는 String channelName; 필드와 getChannelName() 메소드가 있어야 합니다.
        if (video.getChannel() != null && video.getChannel().getChannelName() != null) {
            channelNameStr = video.getChannel().getChannelName();
        }


        return VideoResponseDTO.builder()
                .videoId(video.getVideoId())
                .videoTitle(video.getVideoTitle())
                .videoDescription(video.getVideoDescription())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .region(video.getRegion())
                .city(video.getCity())
                .channelName(channelNameStr)
                .categoryNames(categoryNamesList)
                .tagNames(tagNamesList)
                .views(video.getViews())
                .likes(video.getLikes())
                .build();
    }
}