package com.minute.folder.repository;

import com.minute.folder.entity.Folder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // 👈 @Repository 어노테이션 추가 (선택적이지만 권장)

import java.util.List;
import java.util.Optional; // 👈 Optional import 추가

@Repository // 👈 Spring이 이 인터페이스를 스캔하고 Bean으로 등록하도록 어노테이션 추가
public interface FolderRepository extends JpaRepository<Folder, Integer> {

    // 기존 메소드 (이것도 userId 조건이 추가된 버전이 필요할 수 있습니다.)
    List<Folder> findByFolderNameStartingWith(String prefix);

    // 👇 [새로 추가되어야 할 메소드들] 👇

    // 특정 사용자의 모든 폴더를 생성 시간 역순으로 조회
    List<Folder> findByUserIdOrderByCreatedAtDesc(String userId);

    // 특정 사용자의 특정 폴더 ID로 폴더 조회 (권한 확인 및 수정/삭제 시 사용)
    Optional<Folder> findByFolderIdAndUserId(Integer folderId, String userId);

    // 특정 사용자의 폴더 중 특정 이름으로 시작하는 폴더 목록 조회 (기본 폴더명 생성 시 사용)
    List<Folder> findByUserIdAndFolderNameStartingWith(String userId, String prefix);

    @EntityGraph(attributePaths = {"bookmarks"})
    Optional<Folder> findWithBookmarksByFolderIdAndUserId(Integer folderId, String userId);

}