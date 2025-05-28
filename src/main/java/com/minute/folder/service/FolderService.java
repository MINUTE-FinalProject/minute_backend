package com.minute.folder.service;

import com.minute.folder.entity.Folder;
import com.minute.folder.repository.FolderRepository;
import org.springframework.security.core.Authentication; // 👈 Spring Security import 추가
import org.springframework.security.core.context.SecurityContextHolder; // 👈 Spring Security import 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections; // 👈 Collections import 추가 (getVideosByFolderId 임시 반환용)
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;

    // 현재 로그인한 사용자 ID를 가져오는 헬퍼 메소드
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // 실제 운영 환경에서는 이 부분에 대해 더 강력한 예외 처리나 로직이 필요할 수 있습니다.
            // 예를 들어, 로그인이 필요한 기능에 접근 시 명확한 예외를 발생시켜야 합니다.
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다. 로그인이 필요합니다.");
        }
        // Spring Security의 Principal 객체가 UserDetails를 구현한 커스텀 객체라면,
        // ((YourCustomUserDetails) authentication.getPrincipal()).getUserId() 와 같이 실제 ID를 가져와야 합니다.
        // 기본적으로 authentication.getName()은 username (여기서는 userId)을 반환합니다.
        return authentication.getName();
    }

    @Transactional
    public Folder createFolder(String folderName) {
        String currentUserId = getCurrentUserId(); // 현재 사용자 ID 가져오기

        if (folderName == null || folderName.trim().isEmpty()) {
            folderName = generateDefaultName(currentUserId); // 사용자별 기본 폴더명 생성
        }
        // TODO: 동일 사용자의 폴더 중 이름 중복 체크 로직 추가하면 좋습니다.

        Folder folder = Folder.builder()
                .folderName(folderName)
                .userId(currentUserId) // 👈 생성 시 userId 저장
                .createdAt(LocalDateTime.now())
                .build();

        return folderRepository.save(folder);
    }

    private String generateDefaultName(String userId) {
        String base = "기본폴더";
        // 👇 FolderRepository에 추가한 findByUserIdAndFolderNameStartingWith 사용
        List<Folder> existing = folderRepository.findByUserIdAndFolderNameStartingWith(userId, base);
        int idx = 0;
        while (true) {
            String candidate = idx == 0 ? base : base + idx;
            boolean exists = existing.stream().anyMatch(f -> f.getFolderName().equals(candidate));
            if (!exists) return candidate;
            idx++;
        }
    }

    // 기존 getAll() 대신 현재 사용자의 폴더만 가져오는 메소드로 변경
    public List<Folder> getAllFoldersForCurrentUser() {
        String currentUserId = getCurrentUserId();
        // 👇 FolderRepository에 추가한 findByUserIdOrderByCreatedAtDesc 사용
        return folderRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);
    }

    @Transactional
    public Folder updateName(Integer folderId, String newName) {
        String currentUserId = getCurrentUserId();

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("폴더 이름은 비워둘 수 없습니다.");
        }
        if (newName.length() > 10) { // DTO에서 @Size로 이미 검증했을 수 있지만, 서비스 레벨에서도 방어
            throw new IllegalArgumentException("폴더 이름은 최대 10자까지 가능합니다.");
        }

        // 👇 FolderRepository에 추가한 findByFolderIdAndUserId를 사용하여 해당 폴더가 현재 사용자의 것인지 확인
        Folder folder = folderRepository.findByFolderIdAndUserId(folderId, currentUserId)
                .orElseThrow(() -> new RuntimeException("수정할 폴더를 찾을 수 없거나 해당 폴더에 대한 권한이 없습니다. ID: " + folderId));

        folder.setFolderName(newName);
        // folder.setUpdatedAt(LocalDateTime.now()); // 수정 시간 업데이트가 필요하다면 추가
        return folderRepository.save(folder);
    }

    @Transactional
    public void delete(Integer folderId) {
        String currentUserId = getCurrentUserId();

        // 👇 FolderRepository에 추가한 findByFolderIdAndUserId를 사용하여 해당 폴더가 현재 사용자의 것인지 확인
        Folder folder = folderRepository.findByFolderIdAndUserId(folderId, currentUserId)
                .orElseThrow(() -> new RuntimeException("삭제할 폴더를 찾을 수 없거나 해당 폴더에 대한 권한이 없습니다. ID: " + folderId));

        // folder 객체에서 ID를 가져와서 삭제 (위에서 이미 folder 객체를 가져왔으므로)
        folderRepository.deleteById(folder.getFolderId());
    }

    // 👇 [새로 추가될 메소드 - 폴더 안의 비디오 목록 조회]
    // 이 메소드는 Video 관련 로직이 필요하므로, Video 엔티티, DTO, Repository가 먼저 정의되어야 합니다.
    // 현재는 임시로 빈 목록을 반환하여 /api/folder/{id}/videos API가 401 대신 200 OK를 반환하도록 합니다.
    @Transactional(readOnly = true) // 데이터 변경이 없으므로 읽기 전용 트랜잭션
    public List<?> getVideosByFolderId(Integer folderId) { // TODO: 실제로는 List<VideoDTO> 등을 반환해야 합니다.
        String currentUserId = getCurrentUserId();

        // 1. 요청한 폴더가 현재 사용자의 소유인지 확인
        folderRepository.findByFolderIdAndUserId(folderId, currentUserId)
                .orElseThrow(() -> new RuntimeException("요청한 폴더를 찾을 수 없거나 해당 폴더에 대한 접근 권한이 없습니다. ID: " + folderId));

        // 2. TODO: (향후 작업) VideoRepository 등을 사용하여 folderId에 해당하는 실제 비디오 목록을 조회하고,
        //    VideoDTO 리스트로 변환하여 반환해야 합니다.
        //    예: return videoRepository.findByFolder_FolderIdAndFolder_UserId(folderId, currentUserId)
        //               .stream().map(video -> new VideoDTO(...)).collect(Collectors.toList());

        System.out.println("[FolderService] getVideosByFolderId (임시 응답) 호출, folderId=" + folderId + ", userId=" + currentUserId);
        return Collections.emptyList(); // 현재는 비디오 관련 기능이 없으므로 빈 리스트 반환
    }
}