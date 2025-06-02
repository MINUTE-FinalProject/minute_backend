package com.minute.bookmark.controller;

import com.minute.bookmark.dto.BookmarkCreateRequestDTO;
import com.minute.bookmark.dto.BookmarkResponseDTO;
import com.minute.bookmark.entity.Bookmark; // 서비스 반환 타입이 엔티티일 경우 사용
import com.minute.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.stream.Collectors; // 서비스에서 DTO로 변환하므로 컨트롤러에서는 필요 없을 수 있음

@RestController
@RequestMapping("/api/bookmarks") // API 기본 경로
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BookmarkController {

    private static final Logger log = LoggerFactory.getLogger(BookmarkController.class);
    private final BookmarkService bookmarkService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("인증 정보가 없습니다. 로그인이 필요합니다.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        log.error("알 수 없는 Principal 타입: {}", principal.getClass().getName());
        throw new IllegalStateException("사용자 ID를 확인할 수 없습니다.");
    }

    @PostMapping
    @Operation(summary = "새 북마크 추가 (특정 폴더에 비디오 저장)")
    public ResponseEntity<BookmarkResponseDTO> addBookmark(
            @Valid @RequestBody BookmarkCreateRequestDTO requestDto) {
        String currentUserId = getCurrentUserId();
        log.info("북마크 추가 요청 - 사용자: {}, 요청 DTO: {}", currentUserId, requestDto);

        // BookmarkService의 addVideoToFolder는 이제 Bookmark 엔티티를 반환
        Bookmark savedBookmark = bookmarkService.addVideoToFolder(currentUserId, requestDto);

        // 서비스에서 DTO로 변환하지 않았다면 여기서 변환, 서비스에서 DTO를 반환한다면 그대로 사용
        // 현재 BookmarkService.addVideoToFolder는 Bookmark 엔티티를 반환하므로 여기서 DTO로 변환합니다.
        // 만약 서비스가 BookmarkResponseDTO를 직접 반환하도록 수정했다면, 아래 convertToResponseDto 호출은 필요 없습니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(savedBookmark));
    }

    @DeleteMapping("/{bookmarkId}")
    @Operation(summary = "북마크 ID로 북마크 삭제 (소유권 확인)")
    public ResponseEntity<Void> removeBookmarkByBookmarkId(
            @Parameter(description = "삭제할 북마크의 ID") @PathVariable Integer bookmarkId) {
        String currentUserId = getCurrentUserId();
        log.info("북마크 삭제 요청 (ID 기준) - 사용자: {}, 북마크 ID: {}", currentUserId, bookmarkId);
        bookmarkService.removeBookmarkById(bookmarkId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/folder/{folderId}/video/{videoId}")
    @Operation(summary = "특정 폴더에서 특정 비디오 북마크 삭제")
    public ResponseEntity<Void> removeBookmarkFromFolder(
            @Parameter(description = "비디오가 포함된 폴더의 ID") @PathVariable Integer folderId,
            @Parameter(description = "삭제할 비디오의 ID") @PathVariable String videoId) {
        String currentUserId = getCurrentUserId();
        log.info("폴더 내 북마크 삭제 요청 - 사용자: {}, 폴더 ID: {}, 비디오 ID: {}", currentUserId, folderId, videoId);
        bookmarkService.removeVideoFromUserFolder(currentUserId, folderId, videoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/folder/{folderId}")
    @Operation(summary = "특정 폴더 내의 모든 북마크(비디오) 목록 조회")
    public ResponseEntity<List<BookmarkResponseDTO>> getBookmarksInFolder(
            @Parameter(description = "북마크를 조회할 폴더의 ID") @PathVariable Integer folderId) {
        String currentUserId = getCurrentUserId();
        log.info("폴더 내 북마크 목록 조회 요청 - 사용자: {}, 폴더 ID: {}", currentUserId, folderId);
        List<BookmarkResponseDTO> bookmarks = bookmarkService.getBookmarksByFolder(folderId, currentUserId);
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/user/mine")
    @Operation(summary = "현재 사용자의 모든 북마크 목록 조회")
    public ResponseEntity<List<BookmarkResponseDTO>> getAllMyBookmarks() {
        String currentUserId = getCurrentUserId();
        log.info("현재 사용자({})의 모든 북마크 목록 조회 요청", currentUserId);
        List<BookmarkResponseDTO> bookmarks = bookmarkService.getAllBookmarksForUser(currentUserId);
        return ResponseEntity.ok(bookmarks);
    }

    // Helper method to convert Bookmark entity to BookmarkResponseDTO
    private BookmarkResponseDTO convertToResponseDto(Bookmark bookmark) {
        if (bookmark == null) return null;
        return BookmarkResponseDTO.builder()
                .bookmarkId(bookmark.getBookmarkId())
                .videoId(bookmark.getVideoId())
                .folderId(bookmark.getFolder() != null ? bookmark.getFolder().getFolderId() : null)
                .userId(bookmark.getUserId())
                // .videoTitle(bookmark.getVideoTitle()) // Bookmark 엔티티에 필드가 있다면
                // .thumbnailUrl(bookmark.getThumbnailUrl()) // Bookmark 엔티티에 필드가 있다면
                // .createdAt(bookmark.getCreatedAt()) // Bookmark 엔티티에 필드가 있다면
                .build();
    }
}