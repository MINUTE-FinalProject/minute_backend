package com.minute.folder.repository;

import com.minute.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Integer> {
    // FolderService에서 사용 중인 메소드들
    List<Folder> findByUserIdOrderByCreatedAtDesc(String userId); // 정렬 기준은 createdAt으로 변경 (Folder 엔티티에 맞춤)
    List<Folder> findByUserIdAndFolderNameStartingWith(String userId, String baseName);

    // BookmarkService에서 폴더 소유권 확인을 위해 필요한 메소드
    Optional<Folder> findByFolderIdAndUserId(Integer folderId, String userId);
}