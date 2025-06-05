package com.minute.folder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// ✨ JSON으로 변환 시 null인 필드는 제외하는 옵션 (선택 사항이지만 깔끔합니다)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FolderDTO {

    private Integer folderId;

    @NotBlank(message = "폴더 이름은 비워둘 수 없습니다.")
    @Size(max = 10, message = "폴더 이름은 최대 10자까지 가능합니다.")
    private String folderName;

    // ✨ 랜덤 썸네일 URL을 담을 필드 추가
    private String randomThumbnailUrl;
}