package com.minute.bookmark.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.minute.bookmark.dto.BookmarkCreateRequestDTO;
import com.minute.bookmark.dto.BookmarkResponseDTO;
import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class BookmarkService {

    private static final Logger log = LoggerFactory.getLogger(BookmarkService.class);
    private final BookmarkRepository bookmarkRepository;
    private final FolderRepository folderRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    @Transactional
    public Bookmark addVideoToFolder(String userId, BookmarkCreateRequestDTO requestDto) {
        log.info("[BookmarkService] addVideoToFolder - 사용자 ID: {}, 요청 DTO: {}", userId, requestDto);

        Folder folder = folderRepository.findByFolderIdAndUserId(requestDto.getFolderId(), userId)
                .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없거나 해당 폴더에 대한 접근 권한이 없습니다."));

        String videoUrl = requestDto.getVideoUrl();
        String videoId = extractYouTubeVideoId(videoUrl);
        if (videoId == null) {
            throw new IllegalArgumentException("유효하지 않은 YouTube URL이거나 영상 ID를 추출할 수 없습니다.");
        }

        if (bookmarkRepository.findByUserIdAndVideoIdAndFolder_FolderId(userId, videoId, folder.getFolderId()).isPresent()) {
            throw new IllegalStateException("이미 해당 폴더에 동일한 비디오가 북마크되어 있습니다.");
        }

        JsonNode videoInfo = fetchYouTubeVideoInfo(videoId).block();
        if (videoInfo == null || videoInfo.get("items").isEmpty()) {
            throw new RuntimeException("YouTube에서 영상 정보를 가져올 수 없습니다. ID: " + videoId);
        }

        JsonNode snippet = videoInfo.get("items").get(0).get("snippet");
        String title = snippet.get("title").asText();
        String thumbnailUrl = snippet.path("thumbnails").path("high").path("url").asText();
        if(thumbnailUrl.isEmpty()){
            thumbnailUrl = snippet.path("thumbnails").path("default").path("url").asText();
        }

        Bookmark newBookmark = Bookmark.builder()
                .userId(userId)
                .videoUrl(videoUrl)
                .videoId(videoId)
                .folder(folder)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .build();

        return bookmarkRepository.save(newBookmark);
    }

    private Mono<JsonNode> fetchYouTubeVideoInfo(String videoId) {
        String url = String.format("https://www.googleapis.com/youtube/v3/videos?part=snippet&id=%s&key=%s", videoId, youtubeApiKey);
        return webClientBuilder.build()
                .get()
                .uri(url)
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