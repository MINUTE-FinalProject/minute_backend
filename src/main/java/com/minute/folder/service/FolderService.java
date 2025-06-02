package com.minute.folder.service;

import com.minute.bookmark.dto.BookmarkResponseDTO;
import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.folder.dto.FolderDTO; // ✨ DTO 임포트 추가
import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random; // ✨ Random 임포트 추가
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;
    private static final Logger log = LoggerFactory.getLogger(FolderService.class);

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("[FolderService] getCurrentUserId: Authentication 객체가 null입니다.");
            throw new IllegalStateException("인증 정보를 찾을 수 없습니다. (Auth is null)");
        }
        if (!authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("[FolderService] getCurrentUserId: 사용자가 인증되지 않았습니다.");
            throw new IllegalStateException("인증되지 않은 사용자입니다. (Not Authenticated)");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        throw new IllegalStateException("사용자 ID를 추출할 수 없는 인증 객체 타입입니다.");
    }

    @Transactional
    public Folder createFolder(String folderName) {
        String currentUserId = getCurrentUserId();
        if (folderName == null || folderName.trim().isEmpty()) {
            folderName = generateDefaultName(currentUserId);
        }
        Folder folder = Folder.builder()
                .folderName(folderName)
                .userId(currentUserId)
                .createdAt(LocalDateTime.now())
                .build();
        return folderRepository.save(folder);
    }

    private String generateDefaultName(String userId) {
        String base = "기본폴더";
        List<Folder> existing = folderRepository.findByUserIdAndFolderNameStartingWith(userId, base);
        int idx = 0;
        String candidate;
        while (true) {
            candidate = idx == 0 ? base : base + idx;
            final String finalCandidate = candidate;
            boolean exists = existing.stream().anyMatch(f -> f.getFolderName().equals(finalCandidate));
            if (!exists) {
                return candidate;
            }
            idx++;
        }
    }

    public List<Folder> getAllFoldersForCurrentUser() {
        String currentUserId = getCurrentUserId();
        return folderRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
    }

    // ✨ --- [새로 추가된 메서드] ---
    // 폴더 목록과 함께 랜덤 썸네일을 DTO 리스트로 반환
    @Transactional(readOnly = true)
    public List<FolderDTO> getFoldersWithThumbnailsForCurrentUser() {
        String currentUserId = getCurrentUserId();
        List<Folder> folders = folderRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
        Random random = new Random();

        return folders.stream()
                .map(folder -> {
                    // 1. 각 폴더에 속한 북마크 목록을 조회합니다.
                    // [성능 최적화 제안] 북마크가 매우 많아질 경우,
                    // Repository에 `findRandomBookmarkInFolder` 같은 네이티브 쿼리를 만들어
                    // 딱 하나의 북마크만 가져오는 것이 훨씬 효율적입니다.
                    List<Bookmark> bookmarksInFolder = bookmarkRepository.findByFolder_FolderIdAndUserIdOrderByBookmarkIdDesc(folder.getFolderId(), currentUserId);

                    String thumbnailUrl = null;
                    // 2. 북마크가 존재하면, 그 중 하나를 랜덤으로 선택해 썸네일 URL을 가져옵니다.
                    if (!bookmarksInFolder.isEmpty()) {
                        Bookmark randomBookmark = bookmarksInFolder.get(random.nextInt(bookmarksInFolder.size()));
                        thumbnailUrl = randomBookmark.getThumbnailUrl(); // Bookmark 엔티티에 getThumbnailUrl()이 있다고 가정
                    }

                    // 3. 최종적으로 FolderDTO를 만듭니다.
                    return FolderDTO.builder()
                            .folderId(folder.getFolderId())
                            .folderName(folder.getFolderName())
                            .randomThumbnailUrl(thumbnailUrl) // FolderDTO에 이 필드가 있어야 함
                            .build();
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public Folder updateName(Integer folderId, String newName) {
        String currentUserId = getCurrentUserId();
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("폴더 이름은 비워둘 수 없습니다.");
        }
        if (newName.length() > 10) {
            throw new IllegalArgumentException("폴더 이름은 최대 10자까지 가능합니다.");
        }
        Folder folder = folderRepository.findByFolderIdAndUserId(folderId, currentUserId)
                .orElseThrow(() -> new RuntimeException("수정할 폴더를 찾을 수 없거나 해당 폴더에 대한 권한이 없습니다. ID: " + folderId));
        folder.setFolderName(newName);
        return folderRepository.save(folder);
    }

    @Transactional
    public void delete(Integer folderId) {
        String currentUserId = getCurrentUserId();
        Folder folder = folderRepository.findByFolderIdAndUserId(folderId, currentUserId)
                .orElseThrow(() -> new RuntimeException("삭제할 폴더를 찾을 수 없거나 해당 폴더에 대한 권한이 없습니다. ID: " + folderId));

        // 참고: 폴더를 삭제할 때 해당 폴더에 속한 모든 북마크도 함께 삭제해야 합니다.
        // bookmarkRepository.deleteByFolder(folder); 와 같은 로직 추가를 고려해보세요.
        folderRepository.deleteById(folder.getFolderId());
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getVideosByFolderId(Integer folderId) {
        String currentUserId = getCurrentUserId();
        log.info("[FolderService] getVideosByFolderId 호출 - 사용자 ID: {}, 폴더 ID: {}", currentUserId, folderId);

        folderRepository.findByFolderIdAndUserId(folderId, currentUserId)
                .orElseThrow(() -> {
                    log.warn("[FolderService] getVideosByFolderId: 폴더(ID:{})를 찾을 수 없거나 접근 권한 없음.", folderId);
                    return new RuntimeException("요청한 폴더를 찾을 수 없거나 해당 폴더에 대한 접근 권한이 없습니다.");
                });

        List<Bookmark> bookmarks = bookmarkRepository.findByFolder_FolderIdAndUserIdOrderByBookmarkIdDesc(folderId, currentUserId);
        log.info("[FolderService] getVideosByFolderId: 폴더(ID:{})에서 북마크 {}개 조회됨.", folderId, bookmarks.size());

        return bookmarks.stream()
                .map(BookmarkResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}