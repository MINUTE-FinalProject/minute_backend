package com.minute.bookmark.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkDTO {

    private Integer bookmarkId;

    private String userId;

    @NotBlank(message = "비디오 ID는 필수입니다.")
    private String videoId;

    @NotNull(message = "폴더 ID는 필수입니다.") // 👈 추가: 요청 시 null이 아니어야 함
    private Integer folderId;

}