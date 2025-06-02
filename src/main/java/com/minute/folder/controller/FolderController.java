// com.minute.folder.controller.FolderController.java
package com.minute.folder.controller;

import com.minute.folder.dto.FolderDTO;
// import com.minute.video.dto.VideoDTO; // VideoDTO를 사용한다면 import
import com.minute.folder.service.FolderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderDTO> create(@Valid @RequestBody FolderDTO dto) {
        // 이제 FolderService.createFolder 내부에서 현재 사용자 ID를 사용합니다.
        com.minute.folder.entity.Folder folder = folderService.createFolder(dto.getFolderName());
        FolderDTO responseDto = FolderDTO.builder()
                .folderId(folder.getFolderId())
                .folderName(folder.getFolderName())
                // .userId(folder.getUserId()) // 필요하다면 응답 DTO에 userId도 포함
                .build();
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping // 현재 사용자의 폴더 목록만 반환
    public ResponseEntity<List<FolderDTO>> getAllUserFolders() {
        List<com.minute.folder.entity.Folder> folders = folderService.getAllFoldersForCurrentUser();
        List<FolderDTO> folderDTOs = folders.stream()
                .map(folder -> FolderDTO.builder()
                        .folderId(folder.getFolderId())
                        .folderName(folder.getFolderName())
                        // .userId(folder.getUserId())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(folderDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderDTO> rename(@PathVariable("id") Integer folderId, @Valid @RequestBody FolderDTO dto) {
        com.minute.folder.entity.Folder updated = folderService.updateName(folderId, dto.getFolderName());
        FolderDTO responseDto = FolderDTO.builder()
                .folderId(updated.getFolderId())
                .folderName(updated.getFolderName())
                // .userId(updated.getUserId())
                .build();
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer folderId) {
        folderService.delete(folderId);
        return ResponseEntity.ok().build();
    }

    // 👇 [새로 추가된 API 엔드포인트 - 폴더 안의 비디오 목록 조회]
    @GetMapping("/{folderId}/videos")
    public ResponseEntity<List<?>> getVideosInFolder(@PathVariable Integer folderId) {
        // 반환 타입을 List<VideoDTO> 등으로 변경해야 함
        List<?> videoDTOs = folderService.getVideosByFolderId(folderId);
        return ResponseEntity.ok(videoDTOs);
    }
}