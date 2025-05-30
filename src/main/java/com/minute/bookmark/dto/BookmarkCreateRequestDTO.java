package com.minute.bookmark.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookmarkCreateRequestDTO {

    @NotNull(message = "폴더 ID는 필수입니다.")
    private Integer folderId;

    @NotBlank(message = "비디오 URL은 비어 있을 수 없습니다.")
    private String videoUrl; // 'videoId' 대신 'videoUrl' 필드를 사용합니다.

    // videoTitle, thumbnailUrl 필드는 백엔드에서 직접 생성하므로 DTO에서 제외합니다.
}