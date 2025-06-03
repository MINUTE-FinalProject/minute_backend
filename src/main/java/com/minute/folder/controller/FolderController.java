package com.minute.folder.controller;

import com.minute.folder.dto.FolderDTO;
import com.minute.folder.service.FolderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderDTO> create(@Valid @RequestBody FolderDTO dto) {
        com.minute.folder.entity.Folder folder = folderService.createFolder(dto.getFolderName());
        FolderDTO responseDto = FolderDTO.builder()
                .folderId(folder.getFolderId())
                .folderName(folder.getFolderName())
                .build();
        return ResponseEntity.ok(responseDto);
    }

    // ✨ [수정됨] 폴더 목록 조회 API가 새로운 서비스 메서드를 호출하도록 변경
    @GetMapping
    public ResponseEntity<List<FolderDTO>> getAllUserFolders() {
        // 서비스에서 DTO까지 모두 만들어 반환하므로, 컨트롤러는 받아서 그대로 전달하면 됩니다.
        List<FolderDTO> folderDTOs = folderService.getFoldersWithThumbnailsForCurrentUser();
        return ResponseEntity.ok(folderDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderDTO> rename(@PathVariable("id") Integer folderId, @Valid @RequestBody FolderDTO dto) {
        com.minute.folder.entity.Folder updated = folderService.updateName(folderId, dto.getFolderName());
        FolderDTO responseDto = FolderDTO.builder()
                .folderId(updated.getFolderId())
                .folderName(updated.getFolderName())
                .build();
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer folderId) {
        folderService.delete(folderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{folderId}/videos")
    public ResponseEntity<List<?>> getVideosInFolder(@PathVariable Integer folderId) {
        List<?> videoDTOs = folderService.getVideosByFolderId(folderId);
        return ResponseEntity.ok(videoDTOs);
    }
}