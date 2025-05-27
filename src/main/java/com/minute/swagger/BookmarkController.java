package com.minute.swagger;

import com.minute.bookmark.dto.BookmarkDTO;
import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmark")
@Tag(name = "북마크 API", description = "영상 북마크 추가 및 삭제 기능")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 추가", description = "사용자 ID, 영상 ID, 폴더 ID를 입력하면 북마크를 추가합니다.")
    @PostMapping
    public ResponseEntity<Bookmark> addBookmark(@RequestBody BookmarkDTO bookmarkDTO) {
        Bookmark saved = bookmarkService.add(
                bookmarkDTO.getUserId(),
                bookmarkDTO.getVideoId(),
                bookmarkDTO.getFolderId()
        );
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "북마크 삭제", description = "북마크 ID를 통해 해당 북마크를 삭제합니다.")
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> removeBookmark(
            @Parameter(description = "삭제할 북마크 ID") @PathVariable Integer bookmarkId) {
        bookmarkService.remove(bookmarkId);
        return ResponseEntity.noContent().build();
    }

}