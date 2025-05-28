package com.minute.folder.service;

import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import com.minute.user.entity.User; // User 엔티티 import
import com.minute.user.repository.UserRepository; // UserRepository import (경로는 실제 위치에 맞게 조정)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository; // User 엔티티 조회를 위해 주입
    private static final Logger log = LoggerFactory.getLogger(FolderService.class);

    // 현재 로그인한 사용자 ID를 가져오는 헬퍼 메소드 (변경 없음)
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("[FolderService] getCurrentUserId: Authentication 객체가 null입니다. SecurityContext에 인증 정보가 없습니다.");
            throw new IllegalStateException("인증 정보를 찾을 수 없습니다. 로그인이 필요합니다. (Auth is null)");
        }

        if (!authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("[FolderService] getCurrentUserId: 사용자가 인증되지 않았거나 anonymousUser입니다. Principal: {}", authentication.getPrincipal());
            throw new IllegalStateException("인증되지 않은 사용자입니다. 로그인이 필요합니다. (Not Authenticated or Anonymous)");
        }

        Object principal = authentication.getPrincipal();
        String userId = null;

        if (principal instanceof UserDetails) {
            userId = ((UserDetails) principal).getUsername();
            log.info("[FolderService] getCurrentUserId: UserDetails에서 사용자 ID '{}'를 가져왔습니다.", userId);
        } else if (principal instanceof String) {
            userId = (String) principal;
            log.info("[FolderService] getCurrentUserId: Principal 문자열에서 사용자 ID '{}'를 가져왔습니다.", userId);
        } else {
            log.error("[FolderService] getCurrentUserId: 예상치 못한 Principal 타입입니다. Principal: {}, Type: {}", principal, principal.getClass().getName());
            throw new IllegalStateException("사용자 ID를 추출할 수 없는 인증 객체 타입입니다.");
        }

        if (userId == null || userId.trim().isEmpty()) {
            log.error("[FolderService] getCurrentUserId: 추출된 사용자 ID가 null이거나 비어있습니다.");
            throw new IllegalStateException("유효한 사용자 ID를 가져올 수 없습니다.");
        }

        return userId;
    }

    @Transactional
    public Folder createFolder(String folderName) {
        String currentUserIdStr = getCurrentUserId();
        log.info("[FolderService] createFolder 호출 - 사용자 ID: {}, 폴더명: {}", currentUserIdStr, folderName);

        User currentUser = userRepository.findById(currentUserIdStr)
                .orElseThrow(() -> {
                    log.error("[FolderService] createFolder: User 엔티티를 찾을 수 없습니다. ID: {}", currentUserIdStr);
                    return new RuntimeException("요청한 사용자를 찾을 수 없습니다. ID: " + currentUserIdStr);
                });

        if (folderName == null || folderName.trim().isEmpty()) {
            folderName = generateDefaultName(currentUserIdStr);
            log.info("[FolderService] createFolder: 폴더명이 비어있어 기본 폴더명 '{}'으로 설정합니다.", folderName);
        }

        Folder folder = Folder.builder()
                .folderName(folderName)
                .user(currentUser)
                .build();

        Folder savedFolder = folderRepository.save(folder);
        log.info("[FolderService] createFolder: 폴더 생성 완료. ID: {}", savedFolder.getFolderId());
        return savedFolder;
    }

    private String generateDefaultName(String userId) {
        String base = "기본폴더";
        log.debug("[FolderService] generateDefaultName 시작 - 사용자 ID: {}, 기본명: {}", userId, base);
        // 👇 수정된 Repository 메소드 이름 사용
        List<Folder> existing = folderRepository.findByUser_UserIdAndFolderNameStartingWith(userId, base);
        int idx = 0;
        String candidate;
        while (true) {
            candidate = idx == 0 ? base : base + idx;
            final String finalCandidate = candidate;
            boolean exists = existing.stream().anyMatch(f -> f.getFolderName().equals(finalCandidate));
            if (!exists) {
                log.debug("[FolderService] generateDefaultName: 생성된 기본 폴더명: {}", candidate);
                return candidate;
            }
            idx++;
        }
    }

    public List<Folder> getAllFoldersForCurrentUser() {
        String currentUserId = getCurrentUserId();
        log.info("[FolderService] getAllFoldersForCurrentUser 호출 - 사용자 ID: {}", currentUserId);
        // 👇 수정된 Repository 메소드 이름 사용
        List<Folder> folders = folderRepository.findByUser_UserIdOrderByCreatedAtDesc(currentUserId);
        log.info("[FolderService] getAllFoldersForCurrentUser: 사용자 ID '{}'의 폴더 {}개 조회됨.", currentUserId, folders.size());
        return folders;
    }

    @Transactional
    public Folder updateName(Integer folderId, String newName) {
        String currentUserId = getCurrentUserId();
        log.info("[FolderService] updateName 호출 - 사용자 ID: {}, 폴더 ID: {}, 새 이름: {}", currentUserId, folderId, newName);

        if (newName == null || newName.trim().isEmpty()) {
            log.warn("[FolderService] updateName: 폴더 이름이 비어있습니다.");
            throw new IllegalArgumentException("폴더 이름은 비워둘 수 없습니다.");
        }
        if (newName.length() > 10) {
            log.warn("[FolderService] updateName: 폴더 이름 길이 초과 (10자). 입력된 이름: {}", newName);
            throw new IllegalArgumentException("폴더 이름은 최대 10자까지 가능합니다.");
        }

        // 👇 수정된 Repository 메소드 이름 사용
        Optional<Folder> folderOptional = folderRepository.findByFolderIdAndUser_UserId(folderId, currentUserId);
        if (folderOptional.isEmpty()) {
            log.warn("[FolderService] updateName: 수정할 폴더를 찾을 수 없거나 권한 없음. 폴더 ID: {}, 사용자 ID: {}", folderId, currentUserId);
            throw new RuntimeException("수정할 폴더를 찾을 수 없거나 해당 폴더에 대한 권한이 없습니다. ID: " + folderId);
        }

        Folder folder = folderOptional.get();
        folder.setFolderName(newName);
        Folder updatedFolder = folderRepository.save(folder);
        log.info("[FolderService] updateName: 폴더 이름 변경 완료. 폴더 ID: {}", updatedFolder.getFolderId());
        return updatedFolder;
    }

    @Transactional
    public void delete(Integer folderId) {
        String currentUserId = getCurrentUserId();
        log.info("[FolderService] delete 호출 - 사용자 ID: {}, 폴더 ID: {}", currentUserId, folderId);

        // 👇 수정된 Repository 메소드 이름 사용
        Optional<Folder> folderOptional = folderRepository.findByFolderIdAndUser_UserId(folderId, currentUserId);
        if (folderOptional.isEmpty()) {
            log.warn("[FolderService] delete: 삭제할 폴더를 찾을 수 없거나 권한 없음. 폴더 ID: {}, 사용자 ID: {}", folderId, currentUserId);
            throw new RuntimeException("삭제할 폴더를 찾을 수 없거나 해당 폴더에 대한 권한이 없습니다. ID: " + folderId);
        }

        folderRepository.deleteById(folderOptional.get().getFolderId());
        log.info("[FolderService] delete: 폴더 삭제 완료. 폴더 ID: {}", folderId);
    }

    @Transactional(readOnly = true)
    public List<?> getVideosByFolderId(Integer folderId) {
        String currentUserId = getCurrentUserId();
        log.info("[FolderService] getVideosByFolderId (임시 응답) 호출 - 사용자 ID: {}, 폴더 ID: {}", currentUserId, folderId);

        // 👇 수정된 Repository 메소드 이름 사용
        folderRepository.findByFolderIdAndUser_UserId(folderId, currentUserId)
                .orElseThrow(() -> {
                    log.warn("[FolderService] getVideosByFolderId: 요청한 폴더를 찾을 수 없거나 접근 권한 없음. 폴더 ID: {}, 사용자 ID: {}", folderId, currentUserId);
                    return new RuntimeException("요청한 폴더를 찾을 수 없거나 해당 폴더에 대한 접근 권한이 없습니다. ID: " + folderId);
                });

        log.info("[FolderService] getVideosByFolderId: 사용자 ID '{}', 폴더 ID '{}'에 대한 비디오 목록 (임시로 빈 리스트) 반환.", currentUserId, folderId);
        return Collections.emptyList(); // TODO: 실제 비디오 목록 조회 로직 구현
    }
}