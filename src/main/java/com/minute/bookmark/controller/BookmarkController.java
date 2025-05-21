package com.minute.bookmark.controller;

import com.minute.bookmark.dto.BookmarkDTO;
import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<BookmarkDTO> add(@RequestBody BookmarkDTO dto) {
        Bookmark bookmark = bookmarkService.add(dto.getUserId(), dto.getVideoId(), dto.getFolderId());
        return ResponseEntity.ok(BookmarkDTO.builder()
                .bookmarkId(bookmark.getBookmarkId())
                .userId(bookmark.getUserId())
                .videoId(bookmark.getVideoId())
                .folderId(bookmark.getFolder().getFolderId())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Integer id) {
        bookmarkService.remove(id);
        return ResponseEntity.ok().build();
    }
}