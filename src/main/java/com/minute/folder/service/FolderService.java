package com.minute.folder.service;

import com.minute.bookmark.dto.BookmarkResponseDTO;
import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.folder.dto.FolderDTO;
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
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository; // BookmarkRepository 주입 확인
    private static final Logger log = LoggerFactory.getLogger(FolderService.class);

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("[FolderService] getCurrentUserId: Authentication 객체가 null입니다.");
            throw new RuntimeException("인증 정보를 찾을 수 없습니다. (Authentication is null)");
        }
        if (!authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("[FolderService] getCurrentUserId: 사용자가 인증되지 않았습니다.");
            throw new RuntimeException("인증되지 않은 사용자입니다. (Not Authenticated)");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        log.error("[FolderService] getCurrentUserId: 사용자 ID를 추출할 수 없는 인증 객체 타입: {}", principal.getClass().getName());
        throw new RuntimeException("사용자 ID를 추출할 수 없는 인증 객체 타입입니다.");
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

    // 이 메서드는 더 이상 사용되지 않으므로 제거하거나 private으로 변경하는 것이 좋습니다.
    // public List<Folder> getAllFoldersForCurrentUser() {
    //     String currentUserId = getCurrentUserId();
    //     return folderRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
    // }

    @Transactional(readOnly = true)
    public List<FolderDTO> getFoldersWithThumbnailsForCurrentUser() {
        String currentUserId = getCurrentUserId();
        log.info("[FolderService] getFoldersWithThumbnailsForCurrentUser 호출 - 사용자 ID: {}", currentUserId);

        List<Folder> folders = folderRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
        Random random = new Random();

        return folders.stream()
                .map(folder -> {
                    // 특정 폴더의 북마크를 명시적으로 조회
                    List<Bookmark> bookmarksInFolder = bookmarkRepository.findByFolder_FolderIdAndUserIdOrderByBookmarkIdDesc(folder.getFolderId(), currentUserId);

                    String thumbnailUrl = null;
                    if (bookmarksInFolder != null && !bookmarksInFolder.isEmpty()) {
                        Bookmark randomBookmark = bookmarksInFolder.get(random.nextInt(bookmarksInFolder.size()));
                        thumbnailUrl = randomBookmark.getThumbnailUrl();
                    } else {
                        // 폴더에 북마크가 없는 경우 썸네일을 null로 유지
                        log.info("[FolderService] 폴더(ID:{})에 북마크가 없습니다. 썸네일 없음.", folder.getFolderId());
                    }

                    return FolderDTO.builder()
                            .folderId(folder.getFolderId())
                            .folderName(folder.getFolderName())
                            .randomThumbnailUrl(thumbnailUrl)
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

        // 1. Repository에 추가한, 북마크를 함께 조회하는 전용 메서드를 호출합니다.
        Folder folder = folderRepository.findWithBookmarksByFolderIdAndUserId(folderId, currentUserId)
                .orElseThrow(() -> new RuntimeException("삭제할 폴더를 찾을 수 없거나 해당 폴더에 대한 권한이 없습니다. ID: " + folderId));

        // 2. folder 객체는 이제 연관된 북마크 정보를 모두 가지고 있으므로,
        //    JPA의 CascadeType.REMOVE 규칙에 따라 북마크와 폴더가 함께 삭제됩니다.
        folderRepository.delete(folder);
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