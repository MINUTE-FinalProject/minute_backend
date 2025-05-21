package com.minute.folder.service;

import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;

    public Folder createFolder(String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) {
            folderName = generateDefaultName();
        }

        Folder folder = Folder.builder()
                .folderName(folderName)
                .createdAt(LocalDateTime.now())
                .build();

        return folderRepository.save(folder);
    }

    private String generateDefaultName() {
        String base = "기본폴더";
        List<Folder> existing = folderRepository.findByFolderNameStartingWith(base);
        int idx = 0;

        while (true) {
            String candidate = idx == 0 ? base : base + idx;
            boolean exists = existing.stream().anyMatch(f -> f.getFolderName().equals(candidate));
            if (!exists) return candidate;
            idx++;
        }
    }

    public List<Folder> getAll() {
        return folderRepository.findAll();
    }

    public Folder updateName(Integer folderId, String newName) {
        if (newName.length() > 10) throw new IllegalArgumentException("최대 10자까지 가능합니다.");

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("폴더 없음"));

        folder.setFolderName(newName);
        return folderRepository.save(folder);
    }

    public void delete(Integer folderId) {
        folderRepository.deleteById(folderId);
    }
}