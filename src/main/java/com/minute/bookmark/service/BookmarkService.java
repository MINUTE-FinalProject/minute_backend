package com.minute.bookmark.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minute.bookmark.dto.BookmarkCreateRequestDTO;
import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import com.minute.video.Entity.Video;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.repository.VideoRepository; // VideoRepository 임포트
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BookmarkService {

    private static final Logger log = LoggerFactory.getLogger(BookmarkService.class);
    private final BookmarkRepository bookmarkRepository;
    private final FolderRepository folderRepository;
    private final VideoRepository videoRepository; // VideoRepository 주입
    private final WebClient webClient;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    // 생성자에 VideoRepository 주입 추가
    public BookmarkService(BookmarkRepository bookmarkRepository,
                           FolderRepository folderRepository,
                           VideoRepository videoRepository, // VideoRepository 주입
                           WebClient.Builder webClientBuilder) {
        this.bookmarkRepository = bookmarkRepository;
        this.folderRepository = folderRepository;
        this.videoRepository = videoRepository; // 초기화
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();
    }

    @Transactional
    public Mono<Bookmark> addVideoToFolder(String userId, BookmarkCreateRequestDTO requestDto) {
        log.info("[BookmarkService] addVideoToFolder - 사용자 ID: {}, 요청 DTO: folderId={}, videoId={}",
                userId, requestDto.getFolderId(), requestDto.getVideoId());

        Folder folder = folderRepository.findByFolderIdAndUserId(requestDto.getFolderId(), userId)
                .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없거나 해당 폴더에 대한 접근 권한이 없습니다."));

        String videoIdFromDto = requestDto.getVideoId(); // DTO에서 받은 videoId

        if (videoIdFromDto == null || videoIdFromDto.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("요청에 videoId가 포함되지 않았거나 비어있습니다."));
        }

        String canonicalVideoUrl = "youtu.be" + videoIdFromDto;

        if (bookmarkRepository.findByUserIdAndVideoIdAndFolder_FolderId(userId, videoIdFromDto, folder.getFolderId()).isPresent()) {
            return Mono.error(new IllegalStateException("이미 해당 폴더에 동일한 비디오가 북마크되어 있습니다."));
        }

        // Video 엔티티가 DB에 이미 있는지 확인하고, 없다면 YouTube API를 통해 정보를 가져와서
        // Video 엔티티를 먼저 저장한 후, 그 Video 엔티티를 Bookmark에 연결하는 것이 좋습니다.
        // 현재 코드는 Video 엔티티를 직접 생성/관리하지 않고, videoId만 참조하며,
        // title과 thumbnailUrl은 Bookmark 엔티티에 직접 저장합니다.
        // 만약 Video 엔티티가 중심이라면, 아래 fetchYouTubeVideoInfo 부분은 VideoService로 옮겨지고,
        // 여기서는 videoRepository.findById(videoIdFromDto)를 먼저 호출해야 합니다.
        return fetchYouTubeVideoInfo(videoIdFromDto)
                .flatMap(videoInfo -> {
                    if (videoInfo.path("items").isEmpty()) {
                        return Mono.error(new RuntimeException("YouTube에서 영상 정보를 가져올 수 없습니다. ID: " + videoIdFromDto));
                    }

                    JsonNode snippet = videoInfo.path("items").get(0).path("snippet");
                    String title = snippet.path("title").asText("제목 없음");
                    String thumbnailUrl = snippet.path("thumbnails").path("high").path("url").asText();

                    if (thumbnailUrl.isEmpty()) {
                        thumbnailUrl = snippet.path("thumbnails").path("default").path("url").asText();
                    }

                    Bookmark newBookmark = Bookmark.builder()
                            .userId(userId)
                            .videoUrl(canonicalVideoUrl)
                            .videoId(videoIdFromDto) // Video 엔티티의 ID (String)
                            .folder(folder)
                            .title(title) // 북마크 시점의 제목 (Video 엔티티의 제목과 다를 수 있음)
                            .thumbnailUrl(thumbnailUrl) // 북마크 시점의 썸네일
                            .build();

                    return Mono.just(bookmarkRepository.save(newBookmark));
                });
    }

    private Mono<JsonNode> fetchYouTubeVideoInfo(String videoId) {
        return this.webClient
                .get()
                .uri("/videos?part=snippet&id={videoId}&key={apiKey}", videoId, youtubeApiKey)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(e -> log.error("YouTube API 호출 중 에러 발생, Video ID: {}", videoId, e));
    }

    private String extractYouTubeVideoId(String url) {
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
    public List<VideoResponseDTO> getBookmarksByFolder(Integer folderId, String userId) {
        log.info("[BookmarkService] getBookmarksByFolder - 사용자 ID: {}, 폴더 ID: {}", userId, folderId);
        folderRepository.findByFolderIdAndUserId(folderId, userId)
                .orElseThrow(() -> new RuntimeException("조회하려는 폴더를 찾을 수 없거나 권한이 없습니다."));
        List<Bookmark> bookmarks = bookmarkRepository.findByFolder_FolderIdAndUserIdOrderByBookmarkIdDesc(folderId, userId);

        return bookmarks.stream()
                .map(bookmark -> {
                    if (bookmark.getVideoId() == null) {
                        log.warn("Bookmark ID {} 에 videoId가 null입니다.", bookmark.getBookmarkId());
                        return null;
                    }
                    Optional<Video> videoOptional = videoRepository.findById(bookmark.getVideoId());
                    if (videoOptional.isEmpty()) {
                        log.warn("Video ID {} 에 해당하는 Video 엔티티를 찾을 수 없습니다. (Bookmark ID: {})", bookmark.getVideoId(), bookmark.getBookmarkId());
                        return null;
                    }
                    return VideoResponseDTO.fromEntity(videoOptional.get());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoResponseDTO> getAllBookmarksForUser(String userId) {
        log.info("[BookmarkService] getAllBookmarksForUser - 사용자 ID: {}", userId);
        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByBookmarkIdDesc(userId);

        return bookmarks.stream()
                .map(bookmark -> {
                    if (bookmark.getVideoId() == null) {
                        log.warn("사용자 ID {} 의 북마크 중 videoId가 null인 항목이 있습니다. (Bookmark ID: {})", userId, bookmark.getBookmarkId());
                        return null;
                    }
                    Optional<Video> videoOptional = videoRepository.findById(bookmark.getVideoId());
                    if (videoOptional.isEmpty()) {
                        log.warn("Video ID {} 에 해당하는 Video 엔티티를 찾을 수 없습니다. (Bookmark ID: {})", bookmark.getVideoId(), bookmark.getBookmarkId());
                        return null;
                    }
                    return VideoResponseDTO.fromEntity(videoOptional.get());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}