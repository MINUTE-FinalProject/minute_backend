package com.minute.swagger;

import com.minute.folder.dto.FolderDTO;
import com.minute.folder.entity.Folder;
import com.minute.folder.service.FolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* 현욱 폴더*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/folder")
@Tag(name = "폴더 API", description = "폴더 생성, 조회, 이름 수정, 삭제")
public class FolderController {

    private final FolderService folderService;

    @Operation(summary = "폴더 생성", description = "폴더 이름을 입력하지 않으면 '기본폴더', '기본폴더1' 형식으로 자동 생성됩니다.")
    @PostMapping
    public ResponseEntity<Folder> createFolder(
            @RequestBody(required = false) FolderDTO folderDTO) {

        String name = folderDTO != null ? folderDTO.getFolderName() : null;
        Folder created = folderService.createFolder(name);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "전체 폴더 조회", description = "저장된 모든 폴더 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<List<Folder>> getAllFolders() {
        return ResponseEntity.ok(folderService.getAll());
    }

    @Operation(summary = "폴더 이름 수정", description = "폴더 이름을 수정합니다. (최대 10자까지 가능)")
    @PutMapping("/{folderId}")
    public ResponseEntity<Folder> updateFolderName(
            @Parameter(description = "폴더 ID") @PathVariable Integer folderId,
            @RequestBody FolderDTO folderDTO) {

        Folder updated = folderService.updateName(folderId, folderDTO.getFolderName());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "폴더 삭제", description = "해당 폴더를 삭제합니다.")
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @Parameter(description = "삭제할 폴더 ID") @PathVariable Integer folderId) {
        folderService.delete(folderId);
        return ResponseEntity.noContent().build();
    }
}
