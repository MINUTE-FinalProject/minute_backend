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

    private String videoId;

    private Integer folderId;
}