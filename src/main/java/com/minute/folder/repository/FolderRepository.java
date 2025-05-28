// com.minute.folder.repository.FolderRepository.java
package com.minute.folder.repository;

import com.minute.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional; // Optional import 추가

public interface FolderRepository extends JpaRepository<Folder, Integer> {
    // 기존 메소드 (수정 필요 없을 수 있음, FolderService에서 userId 조건을 추가하여 호출하도록 변경 가능)
    List<Folder> findByFolderNameStartingWith(String prefix);

    // [새로 추가 또는 수정될 메소드 예시]
    // 특정 사용자의 모든 폴더를 생성 시간 역순으로 조회
    List<Folder> findByUserIdOrderByCreatedAtDesc(String userId);

    // 특정 사용자의 특정 폴더 ID로 폴더 조회 (권한 확인 및 수정/삭제 시 사용)
    Optional<Folder> findByFolderIdAndUserId(Integer folderId, String userId);

    // 특정 사용자의 폴더 중 특정 이름으로 시작하는 폴더 목록 조회 (기본 폴더명 생성 시 사용)
    List<Folder> findByUserIdAndFolderNameStartingWith(String userId, String prefix);
}