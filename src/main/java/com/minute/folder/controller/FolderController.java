package com.minute.folder.controller;

import com.minute.folder.dto.FolderDTO;
import com.minute.folder.entity.Folder; // Folder 엔티티 import
import com.minute.folder.service.FolderService;
// import com.minute.video.dto.VideoDTO; // 나중에 실제 VideoDTO를 사용하게 되면 import
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
// import java.util.Collections; // getVideosInFolder에서 FolderService를 호출하므로 직접 사용 안 함

@RestController
@RequestMapping("/api/folder")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderDTO> create(@Valid @RequestBody FolderDTO dto) {
        // FolderService.createFolder 내부에서 현재 사용자 ID를 사용하여 폴더를 생성합니다.
        Folder folder = folderService.createFolder(dto.getFolderName());

        // 응답 DTO에 userId를 포함할지는 선택사항입니다.
        // 여기서는 클라이언트에 userId를 노출하지 않는다고 가정하고, 필요하다면 FolderDTO에 추가하고 매핑합니다.
        FolderDTO responseDto = FolderDTO.builder()
                .folderId(folder.getFolderId())
                .folderName(folder.getFolderName())
                // .userId(folder.getUserId()) // Folder 엔티티에 userId가 있고, DTO에도 있다면 매핑
                .build();

        System.out.println("[FolderController] create 호출 완료, 생성된 폴더 ID: " + folder.getFolderId() + ", 이름: " + folder.getFolderName());
        return ResponseEntity.ok(responseDto);
    }

    // GET /api/folder - 현재 로그인한 사용자의 모든 폴더 목록을 반환합니다.
    @GetMapping
    public ResponseEntity<List<FolderDTO>> getAllUserFolders() {
        // FolderService에서 현재 사용자의 폴더만 가져오는 메소드를 호출합니다.
        List<Folder> folders = folderService.getAllFoldersForCurrentUser();

        List<FolderDTO> folderDTOs = folders.stream()
                .map(folder -> FolderDTO.builder()
                        .folderId(folder.getFolderId())
                        .folderName(folder.getFolderName())
                        // .userId(folder.getUserId()) // DTO에 userId 필드가 있다면 매핑
                        .build())
                .collect(Collectors.toList());

        System.out.println("[FolderController] getAllUserFolders 호출 완료, 조회된 폴더 개수: " + folderDTOs.size());
        return ResponseEntity.ok(folderDTOs);
    }

    @PutMapping("/{folderId}") // 경로 변수 이름을 id -> folderId로 명확히 변경 (선택 사항)
    public ResponseEntity<FolderDTO> rename(@PathVariable Integer folderId, @Valid @RequestBody FolderDTO dto) {
        // FolderService.updateName 내부에서 해당 폴더가 현재 사용자의 것인지 확인합니다.
        Folder updated = folderService.updateName(folderId, dto.getFolderName());

        FolderDTO responseDto = FolderDTO.builder()
                .folderId(updated.getFolderId())
                .folderName(updated.getFolderName())
                // .userId(updated.getUserId())
                .build();

        System.out.println("[FolderController] rename 호출 완료, 수정된 폴더 ID: " + updated.getFolderId() + ", 새 이름: " + updated.getFolderName());
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{folderId}") // 경로 변수 이름을 id -> folderId로 명확히 변경 (선택 사항)
    public ResponseEntity<Void> delete(@PathVariable Integer folderId) {
        // FolderService.delete 내부에서 해당 폴더가 현재 사용자의 것인지 확인합니다.
        folderService.delete(folderId);
        System.out.println("[FolderController] delete 호출 완료, 삭제된 폴더 ID: " + folderId);
        return ResponseEntity.ok().build();
    }

    // GET /api/folder/{folderId}/videos - 특정 폴더 내의 비디오(북마크) 목록을 반환합니다.
    @GetMapping("/{folderId}/videos")
    public ResponseEntity<List<?>> getVideosInFolder(@PathVariable Integer folderId) {
        // FolderService의 getVideosByFolderId가 현재는 임시로 빈 리스트를 반환합니다.
        // 이 메소드 내부에서 해당 folderId가 현재 사용자의 것인지 권한 확인을 수행합니다.
        // TODO: 실제 VideoDTO를 사용하게 되면 반환 타입을 List<VideoDTO>로 변경해야 합니다.
        List<?> videoList = folderService.getVideosByFolderId(folderId);

        System.out.println("[FolderController] getVideosInFolder 호출, folderId=" + folderId + ", 조회된 비디오(임시) 개수: " + videoList.size());
        return ResponseEntity.ok(videoList);
    }
}