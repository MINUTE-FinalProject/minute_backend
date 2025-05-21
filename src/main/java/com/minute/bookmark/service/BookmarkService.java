package com.minute.bookmark.service;

import com.minute.bookmark.entity.Bookmark;
import com.minute.bookmark.repository.BookmarkRepository;
import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final FolderRepository folderRepository;

    public Bookmark add(String userId, String videoId, Integer folderId) {
        if (bookmarkRepository.findByUserIdAndVideoIdAndFolder_FolderId(userId, videoId, folderId).isPresent()) {
            throw new RuntimeException("이미 존재");
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("폴더 없음"));

        Bookmark bookmark = Bookmark.builder()
                .userId(userId)
                .videoId(videoId)
                .folder(folder)
                .build();

        return bookmarkRepository.save(bookmark);
    }

    public void remove(Integer bookmarkId) {
        bookmarkRepository.deleteById(bookmarkId);
    }
}