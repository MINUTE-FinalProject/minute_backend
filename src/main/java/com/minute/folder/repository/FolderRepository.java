// com.minute.folder.repository.FolderRepository.java
package com.minute.folder.repository;

import com.minute.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Integer> {

    // 이 메소드는 현재 FolderService에서 사용되지 않으므로 그대로 두거나 필요에 따라 수정합니다.
    // 만약 사용자별로 필터링해야 한다면 이 메소드도 User_UserId를 포함하도록 변경해야 합니다.
    List<Folder> findByFolderNameStartingWith(String prefix);

    // --- 수정된 메소드 이름 ---
    // 특정 사용자의 모든 폴더를 생성 시간 역순으로 조회
    List<Folder> findByUser_UserIdOrderByCreatedAtDesc(String userId); // 변경: User_UserId

    // 특정 사용자의 특정 폴더 ID로 폴더 조회 (권한 확인 및 수정/삭제 시 사용)
    Optional<Folder> findByFolderIdAndUser_UserId(Integer folderId, String userId); // 변경: User_UserId

    // 특정 사용자의 폴더 중 특정 이름으로 시작하는 폴더 목록 조회 (기본 폴더명 생성 시 사용)
    List<Folder> findByUser_UserIdAndFolderNameStartingWith(String userId, String prefix); // 변경: User_UserId
}