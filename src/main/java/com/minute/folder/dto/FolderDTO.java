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

    @Size(max = 10)
    private String folderName;
}