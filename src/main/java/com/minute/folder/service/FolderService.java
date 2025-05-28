package com.minute.folder.service;

import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import org.slf4j.Logger; // 👈 SLF4J Logger import 추가
import org.slf4j.LoggerFactory; // 👈 SLF4J Logger import 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // 👈 UserDetails import 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional; // 👈 Optional import 추가 (findByIdAndUserId 반환 타입 일치)

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private static final Logger log = LoggerFactory.getLogger(FolderService.class); // 👈 로거 선언

    // 현재 로그인한 사용자 ID를 가져오는 헬퍼 메소드 (개선)
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
            // Spring Security의 UserDetails 인터페이스를 구현한 경우 (일반적)
            userId = ((UserDetails) principal).getUsername();
            log.info("[FolderService] getCurrentUserId: UserDetails에서 사용자 ID '{}'를 가져왔습니다.", userId);
        } else if (principal instanceof String) {
            // Principal이 단순 문자열인 경우 (예: 직접 설정한 경우)
            userId = (String) principal;
            log.info("[FolderService] getCurrentUserId: Principal 문자열에서 사용자 ID '{}'를 가져왔습니다.", userId);
        } else {
            // 예상치 못한 Principal 타입
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
        String currentUserId = getCurrentUserId(); // 인증 확인 포함
        log.info("[FolderService] createFolder 호출 - 사용자 ID: {}, 폴더명: {}", currentUserId, folderName);

        if (folderName == null || folderName.trim().isEmpty()) {
            folderName = generateDefaultName(currentUserId);
            log.info("[FolderService] createFolder: 폴더명이 비어있어 기본 폴더명 '{}'으로 설정합니다.", folderName);
        }
        // TODO: 동일 사용자의 폴더 중 이름 중복 체크 로직 추가하면 좋음

        Folder folder = Folder.builder()
                .folderName(folderName)
                .userId(currentUserId)
                .createdAt(LocalDateTime.now())
                .build();

        Folder savedFolder = folderRepository.save(folder);
        log.info("[FolderService] createFolder: 폴더 생성 완료. ID: {}", savedFolder.getFolderId());
        return savedFolder;
    }

    private String generateDefaultName(String userId) {
        String base = "기본폴더";
        log.debug("[FolderService] generateDefaultName 시작 - 사용자 ID: {}, 기본명: {}", userId, base);
        List<Folder> existing = folderRepository.findByUserIdAndFolderNameStartingWith(userId, base);
        int idx = 0;
        String candidate;
        while (true) {
            candidate = idx == 0 ? base : base + idx;
            final String finalCandidate = candidate; // 람다식 내부에서 사용하기 위해 effectively final 변수 사용
            boolean exists = existing.stream().anyMatch(f -> f.getFolderName().equals(finalCandidate));
            if (!exists) {
                log.debug("[FolderService] generateDefaultName: 생성된 기본 폴더명: {}", candidate);
                return candidate;
            }
            idx++;
        }
    }

    public List<Folder> getAllFoldersForCurrentUser() {
        String currentUserId = getCurrentUserId(); // 인증 확인 포함
        log.info("[FolderService] getAllFoldersForCurrentUser 호출 - 사용자 ID: {}", currentUserId);
        List<Folder> folders = folderRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
        log.info("[FolderService] getAllFoldersForCurrentUser: 사용자 ID '{}'의 폴더 {}개 조회됨.", currentUserId, folders.size());
        return folders;
    }

    @Transactional
    public Folder updateName(Integer folderId, String newName) {
        String currentUserId = getCurrentUserId(); // 인증 확인 포함
        log.info("[FolderService] updateName 호출 - 사용자 ID: {}, 폴더 ID: {}, 새 이름: {}", currentUserId, folderId, newName);

        if (newName == null || newName.trim().isEmpty()) {
            log.warn("[FolderService] updateName: 폴더 이름이 비어있습니다.");
            throw new IllegalArgumentException("폴더 이름은 비워둘 수 없습니다.");
        }
        if (newName.length() > 10) {
            log.warn("[FolderService] updateName: 폴더 이름 길이 초과 (10자). 입력된 이름: {}", newName);
            throw new IllegalArgumentException("폴더 이름은 최대 10자까지 가능합니다.");
        }

        Optional<Folder> folderOptional = folderRepository.findByFolderIdAndUserId(folderId, currentUserId);
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
        String currentUserId = getCurrentUserId(); // 인증 확인 포함
        log.info("[FolderService] delete 호출 - 사용자 ID: {}, 폴더 ID: {}", currentUserId, folderId);

        Optional<Folder> folderOptional = folderRepository.findByFolderIdAndUserId(folderId, currentUserId);
        if (folderOptional.isEmpty()) {
            log.warn("[FolderService] delete: 삭제할 폴더를 찾을 수 없거나 권한 없음. 폴더 ID: {}, 사용자 ID: {}", folderId, currentUserId);
            throw new RuntimeException("삭제할 폴더를 찾을 수 없거나 해당 폴더에 대한 권한이 없습니다. ID: " + folderId);
        }

        folderRepository.deleteById(folderOptional.get().getFolderId());
        log.info("[FolderService] delete: 폴더 삭제 완료. 폴더 ID: {}", folderId);
    }

    @Transactional(readOnly = true)
    public List<?> getVideosByFolderId(Integer folderId) { // TODO: 반환 타입을 List<VideoDTO> 등으로 변경
        String currentUserId = getCurrentUserId(); // 인증 확인 포함
        log.info("[FolderService] getVideosByFolderId (임시 응답) 호출 - 사용자 ID: {}, 폴더 ID: {}", currentUserId, folderId);

        // 요청한 폴더가 현재 사용자의 소유인지 확인 (존재하지 않거나 권한 없으면 예외 발생)
        folderRepository.findByFolderIdAndUserId(folderId, currentUserId)
                .orElseThrow(() -> {
                    log.warn("[FolderService] getVideosByFolderId: 요청한 폴더를 찾을 수 없거나 접근 권한 없음. 폴더 ID: {}, 사용자 ID: {}", folderId, currentUserId);
                    return new RuntimeException("요청한 폴더를 찾을 수 없거나 해당 폴더에 대한 접근 권한이 없습니다. ID: " + folderId);
                });

        // TODO: (향후 작업) VideoRepository 등을 사용하여 실제 비디오 목록 조회 로직 구현
        log.info("[FolderService] getVideosByFolderId: 사용자 ID '{}', 폴더 ID '{}'에 대한 비디오 목록 (임시로 빈 리스트) 반환.", currentUserId, folderId);
        return Collections.emptyList();
    }
}