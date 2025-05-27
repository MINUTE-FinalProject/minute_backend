package com.minute.folder.controller;

import com.minute.folder.dto.FolderDTO;
import com.minute.folder.entity.Folder;
import com.minute.folder.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderDTO> create(@RequestBody FolderDTO dto) {
        Folder folder = folderService.createFolder(dto.getFolderName());
        return ResponseEntity.ok(FolderDTO.builder()
                .folderId(folder.getFolderId())
                .folderName(folder.getFolderName())
                .build());
    }

    @GetMapping
    public ResponseEntity<List<Folder>> all() {
        return ResponseEntity.ok(folderService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderDTO> rename(@PathVariable Integer id, @RequestBody FolderDTO dto) {
        Folder updated = folderService.updateName(id, dto.getFolderName());
        return ResponseEntity.ok(FolderDTO.builder()
                .folderId(updated.getFolderId())
                .folderName(updated.getFolderName())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        folderService.delete(id);
        return ResponseEntity.ok().build();
    }
}