package com.minute.bookmark.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minute.bookmark.dto.BookmarkCreateRequestDTO;
import com.minute.bookmark.dto.BookmarkResponseDTO;
import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private static final Logger log = LoggerFactory.getLogger(BookmarkService.class);
    private final BookmarkRepository bookmarkRepository;
    private final FolderRepository folderRepository;
    private final WebClient webClient; // Builder가 아닌 WebClient 인스턴스를 직접 필드로 가집니다.

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    // @RequiredArgsConstructor 대신 생성자를 직접 작성하여 WebClient를 빌드합니다.
    public BookmarkService(BookmarkRepository bookmarkRepository,
                           FolderRepository folderRepository,
                           WebClient.Builder webClientBuilder) {
        this.bookmarkRepository = bookmarkRepository;
        this.folderRepository = folderRepository;
        // WebClient 인스턴스를 서비스가 생성될 때 한 번만 만듭니다.
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();
    }

    /**
     * 북마크 추가 메서드 (리액티브 방식으로 변경)
     * 이제 Bookmark 엔티티를 Mono로 감싸서 반환합니다.
     */
    @Transactional
    public Mono<Bookmark> addVideoToFolder(String userId, BookmarkCreateRequestDTO requestDto) {
        // ✨ 로그 메시지에서 DTO 필드 접근 방식 수정
        log.info("[BookmarkService] addVideoToFolder - 사용자 ID: {}, 요청 DTO: folderId={}, videoId={}",
                userId, requestDto.getFolderId(), requestDto.getVideoId());

        // 1. 동기적으로 처리 가능한 로직은 먼저 수행합니다.
        Folder folder = folderRepository.findByFolderIdAndUserId(requestDto.getFolderId(), userId)
                .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없거나 해당 폴더에 대한 접근 권한이 없습니다."));

        // ✨ [수정 1] DTO에서 videoId를 직접 가져옵니다.
        String videoId = requestDto.getVideoId();

        // ✨ [수정 2] videoId가 null이거나 비어있는지 직접 확인합니다.
        if (videoId == null || videoId.trim().isEmpty()) {
            // 이제 @NotBlank 등으로 DTO 레벨에서 검증되므로, 이 에러는 거의 발생하지 않아야 합니다.
            return Mono.error(new IllegalArgumentException("요청에 videoId가 포함되지 않았거나 비어있습니다."));
        }

        // ✨ [수정 3] DB에 저장할 표준 videoUrl 생성 (예시: YouTube 기본 시청 URL)
        String canonicalVideoUrl = "youtu.be" + videoId;

        if (bookmarkRepository.findByUserIdAndVideoIdAndFolder_FolderId(userId, videoId, folder.getFolderId()).isPresent()) {
            return Mono.error(new IllegalStateException("이미 해당 폴더에 동일한 비디오가 북마크되어 있습니다."));
        }

        // 2. WebClient 호출부터 리액티브 체인으로 구성합니다.
        return fetchYouTubeVideoInfo(videoId)
                .flatMap(videoInfo -> {
                    // .path()를 사용하여 NullPointerException을 방지합니다.
                    if (videoInfo.path("items").isEmpty()) {
                        return Mono.error(new RuntimeException("YouTube에서 영상 정보를 가져올 수 없습니다. ID: " + videoId));
                    }

                    JsonNode snippet = videoInfo.path("items").get(0).path("snippet");
                    String title = snippet.path("title").asText("제목 없음"); // 제목이 없을 경우 기본값 설정
                    String thumbnailUrl = snippet.path("thumbnails").path("high").path("url").asText();

                    if (thumbnailUrl.isEmpty()) {
                        thumbnailUrl = snippet.path("thumbnails").path("default").path("url").asText();
                    }

                    Bookmark newBookmark = Bookmark.builder()
                            .userId(userId)
                            .videoUrl(canonicalVideoUrl) // ✨ [수정 4] 생성된 표준 URL 또는 원본 URL 사용
                            .videoId(videoId)
                            .folder(folder)
                            .title(title)
                            .thumbnailUrl(thumbnailUrl)
                            .build();

                    // DB에 저장하는 작업도 Mono 안에서 수행합니다.
                    return Mono.just(bookmarkRepository.save(newBookmark));
                });
    }

    /**
     * 재사용 가능한 WebClient 인스턴스를 사용하도록 수정
     */
    private Mono<JsonNode> fetchYouTubeVideoInfo(String videoId) {
        return this.webClient // 미리 생성된 webClient 사용
                .get()
                // baseUrl이 설정되어 있으므로 상대 경로만 지정합니다.
                .uri("/videos?part=snippet&id={videoId}&key={apiKey}", videoId, youtubeApiKey)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(e -> log.error("YouTube API 호출 중 에러 발생, Video ID: {}", videoId, e));
    }

    // 이 메서드는 현재 addVideoToFolder에서 직접 사용되지 않지만, 다른 로직에서 사용될 수 있으므로 유지합니다.
    private String extractYouTubeVideoId(String url) {
        // 기존 정규식 패턴은 유지
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed\\%2Fvideos\\%2F|youtu.be%2F|\\/v\\/)[^#\\&\\?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    @Transactional
    public void removeBookmarkById(Integer bookmarkId, String userId) {
        log.info("[BookmarkService] removeBookmarkById - 사용자 ID: {}, 북마크 ID: {}", userId, bookmarkId);
        Bookmark bookmark = bookmarkRepository.findByBookmarkIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> new RuntimeException("삭제할 북마크를 찾을 수 없거나 권한이 없습니다."));
        bookmarkRepository.delete(bookmark);
        log.info("[BookmarkService] removeBookmarkById: 북마크(ID:{}) 삭제 완료.", bookmarkId);
    }

    @Transactional
    public void removeVideoFromUserFolder(String userId, Integer folderId, String videoId) {
        log.info("[BookmarkService] removeVideoFromUserFolder - 사용자 ID: {}, 폴더 ID: {}, 비디오 ID: {}", userId, folderId, videoId);
        folderRepository.findByFolderIdAndUserId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("해당 폴더를 찾을 수 없거나 권한이 없습니다."));
        bookmarkRepository.deleteByFolder_FolderIdAndVideoIdAndUserId(folderId, videoId, userId);
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getBookmarksByFolder(Integer folderId, String userId) {
        log.info("[BookmarkService] getBookmarksByFolder - 사용자 ID: {}, 폴더 ID: {}", userId, folderId);
        folderRepository.findByFolderIdAndUserId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("조회하려는 폴더를 찾을 수 없거나 권한이 없습니다."));
        List<Bookmark> bookmarks = bookmarkRepository.findByFolder_FolderIdAndUserIdOrderByBookmarkIdDesc(folderId, userId);

        return bookmarks.stream()
                .map(BookmarkResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookmarkResponseDTO> getAllBookmarksForUser(String userId) {
        log.info("[BookmarkService] getAllBookmarksForUser - 사용자 ID: {}", userId);
        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByBookmarkIdDesc(userId);

        return bookmarks.stream()
                .map(BookmarkResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}