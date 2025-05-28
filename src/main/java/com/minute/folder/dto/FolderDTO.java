package com.minute.folder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderDTO {

    private Integer folderId;

    @NotBlank(message = "폴더 이름은 비워둘 수 없습니다.")
    @Size(max = 10, message = "폴더 이름은 최대 10자까지 가능합니다.")
    private String folderName;
}