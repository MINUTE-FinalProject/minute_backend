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

    // ✨ [수정] 프론트에서 보내주는 'videoId'와 이름을 일치시킵니다.
    @NotBlank(message = "비디오 ID는 필수입니다.")
    private String videoId;
}