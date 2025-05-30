package com.minute.bookmark.service;

import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// DTOs
import com.minute.bookmark.dto.BookmarkCreateRequestDTO;
import com.minute.bookmark.dto.BookmarkResponseDTO;


import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookmarkService {

    private static final Logger log = LoggerFactory.getLogger(BookmarkService.class);
    private final BookmarkRepository bookmarkRepository;
    private final FolderRepository folderRepository;

    @Transactional
    public Bookmark addVideoToFolder(String userId, BookmarkCreateRequestDTO requestDto /*, String videoTitle, String thumbnailUrl - 필요시 추가 */) {
        log.info("[BookmarkService] addVideoToFolder - 사용자 ID: {}, 요청 DTO: {}", userId, requestDto);

        Folder folder = folderRepository.findByFolderIdAndUserId(requestDto.getFolderId(), userId)
                .orElseThrow(() -> {
                    log.warn("[BookmarkService] addVideoToFolder: 폴더(ID:{})를 찾을 수 없거나 사용자({})에게 권한 없음.", requestDto.getFolderId(), userId);
                    return new RuntimeException("폴더를 찾을 수 없거나 해당 폴더에 대한 접근 권한이 없습니다. 폴더 ID: " + requestDto.getFolderId());
                });

        if (bookmarkRepository.findByUserIdAndVideoIdAndFolder_FolderId(userId, requestDto.getVideoId(), folder.getFolderId()).isPresent()) {
            log.warn("[BookmarkService] addVideoToFolder: 이미 해당 폴더(ID:{})에 비디오(ID:{})가 북마크되어 있습니다. 사용자 ID: {}", folder.getFolderId(), requestDto.getVideoId(), userId);
            throw new IllegalStateException("이미 해당 폴더에 동일한 비디오가 북마크되어 있습니다.");
        }

        Bookmark newBookmark = Bookmark.builder()
                .userId(userId)
                .videoId(requestDto.getVideoId())
                .folder(folder)
                // Bookmark 엔티티에 videoTitle, thumbnailUrl, createdAt 등이 있고, DTO에서 받는다면 여기서 설정
                // .videoTitle(requestDto.getVideoTitle())
                // .thumbnailUrl(requestDto.getThumbnailUrl())
                .build();
        try {
            return bookmarkRepository.save(newBookmark);
        } catch (DataIntegrityViolationException e) {
            log.error("[BookmarkService] addVideoToFolder: 데이터 무결성 위반. 사용자 ID: {}, 폴더 ID: {}, 비디오 ID: {}", userId, requestDto.getFolderId(), requestDto.getVideoId(), e);
            throw new IllegalStateException("북마크 저장 중 오류가 발생했습니다. 이미 존재하는 북마크일 수 있습니다.", e);
        }
    }

    @Transactional
    public void removeBookmarkById(Integer bookmarkId, String userId) {
        log.info("[BookmarkService] removeBookmarkById - 사용자 ID: {}, 북마크 ID: {}", userId, bookmarkId);
        Bookmark bookmark = bookmarkRepository.findByBookmarkIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> {
                    log.warn("[BookmarkService] removeBookmarkById: 북마크(ID:{})를 찾을 수 없거나 사용자({})에게 권한 없음.", bookmarkId, userId);
                    return new RuntimeException("삭제할 북마크를 찾을 수 없거나 권한이 없습니다. 북마크 ID: " + bookmarkId);
                });
        bookmarkRepository.delete(bookmark);
        log.info("[BookmarkService] removeBookmarkById: 북마크(ID:{}) 삭제 완료.", bookmarkId);
    }

    @Transactional
    public void removeVideoFromUserFolder(String userId, Integer folderId, String videoId) {
        log.info("[BookmarkService] removeVideoFromUserFolder - 사용자 ID: {}, 폴더 ID: {}, 비디오 ID: {}", userId, folderId, videoId);
        folderRepository.findByFolderIdAndUserId(folderId, userId) // 폴더 소유권 먼저 확인
                .orElseThrow(() -> new RuntimeException("해당 폴더를 찾을 수 없거나 권한이 없습니다."));

        long deleteCount = bookmarkRepository.deleteByFolder_FolderIdAndVideoIdAndUserId(folderId, videoId, userId);
        if (deleteCount == 0) {
            log.warn("[BookmarkService] removeVideoFromUserFolder: 삭제할 북마크를 찾지 못했습니다. 사용자: {}, 폴더: {}, 비디오: {}", userId, folderId, videoId);
        } else {
            log.info("[BookmarkService] removeVideoFromUserFolder: 북마크 삭제 완료. 삭제된 수: {}", deleteCount);
        }
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getBookmarksByFolder(Integer folderId, String userId) {
        log.info("[BookmarkService] getBookmarksByFolder - 사용자 ID: {}, 폴더 ID: {}", userId, folderId);
        folderRepository.findByFolderIdAndUserId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("조회하려는 폴더를 찾을 수 없거나 권한이 없습니다."));

        List<Bookmark> bookmarks = bookmarkRepository.findByFolder_FolderIdAndUserIdOrderByBookmarkIdDesc(folderId, userId);
        return bookmarks.stream()
                .map(this::convertToResponseDto) // DTO 변환 헬퍼 메소드 사용
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getAllBookmarksForUser(String userId) {
        log.info("[BookmarkService] getAllBookmarksForUser - 사용자 ID: {}", userId);
        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByBookmarkIdDesc(userId);
        return bookmarks.stream()
                .map(this::convertToResponseDto) // DTO 변환 헬퍼 메소드 사용
                .collect(Collectors.toList());
    }

    // Helper method to convert Bookmark entity to BookmarkResponseDTO
    private BookmarkResponseDTO convertToResponseDto(Bookmark bookmark) {
        if (bookmark == null) return null;
        return BookmarkResponseDTO.builder()
                .bookmarkId(bookmark.getBookmarkId())
                .videoId(bookmark.getVideoId())
                .folderId(bookmark.getFolder() != null ? bookmark.getFolder().getFolderId() : null)
                .userId(bookmark.getUserId())
                // Bookmark 엔티티에 videoTitle, thumbnailUrl, createdAt 등이 있다면 여기서 매핑
                // .videoTitle(bookmark.getVideoTitle())
                // .thumbnailUrl(bookmark.getThumbnailUrl())
                // .createdAt(bookmark.getCreatedAt())
                .build();
    }
}