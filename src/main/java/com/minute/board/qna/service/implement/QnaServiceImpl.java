package com.minute.board.qna.service.implement;

import com.minute.board.qna.dto.request.QnaCreateRequestDTO;
import com.minute.board.qna.dto.response.QnaAttachmentResponseDTO;
import com.minute.board.qna.dto.response.QnaDetailResponseDTO;
import com.minute.board.qna.dto.response.QnaReplyResponseDTO;
import com.minute.board.qna.dto.response.QnaSummaryResponseDTO;
import com.minute.board.qna.entity.*;
import com.minute.board.qna.repository.QnaAttachmentRepository;
import com.minute.board.qna.repository.QnaReportRepository;
import com.minute.board.qna.repository.QnaRepository;
import com.minute.board.qna.service.QnaService;
import com.minute.common.file.service.FileStorageService; // FileStorageService 인터페이스
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException; // 권한 예외
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // StringUtils 임포트
import org.springframework.web.multipart.MultipartFile;

import com.minute.board.qna.dto.request.QnaReplyRequestDTO; // 추가
import com.minute.board.qna.dto.response.AdminQnaDetailResponseDTO; // 추가
import com.minute.board.qna.dto.response.AdminQnaSummaryResponseDTO; // 추가
import com.minute.board.qna.dto.response.QnaReplyResponseDTO; // 추가
import com.minute.board.qna.repository.QnaReplyRepository; // 추가
import org.springframework.data.jpa.domain.Specification; // Specification 추가 (동적 쿼리용)
import com.minute.board.qna.dto.request.QnaUpdateRequestDTO; // 추가
import com.minute.board.qna.dto.response.QnaReportResponseDTO; // 추가

import com.minute.board.qna.dto.response.ReportedQnaItemResponseDTO; // 추가
import com.minute.board.qna.entity.QnaReport; // 추가

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDate; // 추가
import java.time.LocalDateTime; // 추가
import java.time.LocalTime; // 추가

import java.util.Optional; // 추가

@Slf4j
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션, 쓰기 작업 메서드에 @Transactional 추가
public class QnaServiceImpl implements QnaService {

    private final QnaRepository qnaRepository;
    private final QnaAttachmentRepository qnaAttachmentRepository;
    private final UserRepository userRepository;
    private final QnaReplyRepository qnaReplyRepository; // 추가
    private final QnaReportRepository qnaReportRepository; // 추가

    @Qualifier("s3FileStorageService") // 특정 빈 이름 지정 (S3FileStorageService에 @Service("s3FileStorageService") 설정 필요)
    private final FileStorageService fileStorageService; // S3 서비스 주입

    private static final String QNA_FILE_SUBDIRECTORY = "qna"; // S3 내 QnA 파일 저장 경로

    @Override
    @Transactional // 쓰기 작업이므로 클래스 레벨의 readOnly=true를 오버라이드
    public QnaDetailResponseDTO createQna(QnaCreateRequestDTO requestDTO, List<MultipartFile> files, String userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        Qna qna = Qna.builder()
                .inquiryTitle(requestDTO.getInquiryTitle())
                .inquiryContent(requestDTO.getInquiryContent())
                .user(user)
                .inquiryStatus(QnaStatus.PENDING) // 기본 상태 PENDING
                .attachments(new ArrayList<>()) // NullPointerException 방지
                .build();
        Qna savedQna = qnaRepository.save(qna);

        List<QnaAttachmentResponseDTO> attachmentDTOs = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            // S3에 파일 업로드 및 QnaAttachment 엔티티 생성/저장
            List<String> uploadedFileUrls = fileStorageService.uploadFiles(files, QNA_FILE_SUBDIRECTORY);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String fileUrl = uploadedFileUrls.get(i); // uploadFiles가 URL 목록을 순서대로 반환한다고 가정

                QnaAttachment attachment = QnaAttachment.builder()
                        .qna(savedQna)
                        .imgFilePath(fileUrl) // S3에서 반환된 전체 URL 저장
                        .imgOriginalFilename(file.getOriginalFilename())
                        .imgSavedFilename(extractKeyFromUrl(fileUrl)) // URL에서 Key 부분만 추출하거나, S3 서비스가 Key도 반환하도록 수정
                        .build();
                qnaAttachmentRepository.save(attachment);
                savedQna.getAttachments().add(attachment); // Qna 엔티티의 attachments 리스트에도 추가 (양방향 연관관계 편의상)

                // 응답 DTO용 첨부파일 정보 생성
                attachmentDTOs.add(QnaAttachmentResponseDTO.builder()
                        .imgId(attachment.getImgId())
                        .fileUrl(attachment.getImgFilePath())
                        .originalFilename(attachment.getImgOriginalFilename())
                        .createdAt(attachment.getImgCreatedAt())
                        .build());
            }
        }

        // 생성된 QnA 상세 정보를 DTO로 변환하여 반환
        return QnaDetailResponseDTO.builder()
                .inquiryId(savedQna.getInquiryId())
                .inquiryTitle(savedQna.getInquiryTitle())
                .inquiryContent(savedQna.getInquiryContent())
                .authorNickname(user.getUserNickName())
                .inquiryStatus(savedQna.getInquiryStatus().name())
                .inquiryCreatedAt(savedQna.getInquiryCreatedAt())
                .inquiryUpdatedAt(savedQna.getInquiryUpdatedAt())
                .attachments(attachmentDTOs)
                .reply(null) // 새로 생성된 문의에는 답변이 없음
                .build();
    }

    // QnaServiceImpl.java 내 getMyQnas 메서드 수정 예시
    @Override
    public Page<QnaSummaryResponseDTO> getMyQnas(String userId, Pageable pageable, String searchTerm,
                                                 String statusFilter, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        Specification<Qna> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user"), user));

            if (StringUtils.hasText(searchTerm)) {
                String likePattern = "%" + searchTerm + "%"; // searchTerm 자체를 사용 (DB가 대소문자 구분 안하게 설정되었거나, 구분 감수)
                String lowerSearchTermPattern = "%" + searchTerm.toLowerCase() + "%"; // 제목은 소문자로 변환하여 비교

                Predicate titleMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("inquiryTitle")), // 제목은 VARCHAR이므로 LCASE/LOWER 가능
                        lowerSearchTermPattern
                );

                // inquiryContent (CLOB)는 LOWER 함수 없이 직접 LIKE 검색
                // DB의 collation 설정이 대소문자를 구분하지 않는다면 (예: _ci 접미사), 잘 동작할 수 있음
                Predicate contentMatch = criteriaBuilder.like(
                        root.get("inquiryContent"),
                        likePattern
                );

                predicates.add(criteriaBuilder.or(titleMatch, contentMatch));
            }

            if (StringUtils.hasText(statusFilter)) {
                try {
                    QnaStatus qnaStatus = QnaStatus.valueOf(statusFilter.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("inquiryStatus"), qnaStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid QnaStatus filter value for user QnA list: {}", statusFilter);
                }
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("inquiryCreatedAt"), LocalDateTime.of(startDate, LocalTime.MIN)));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("inquiryCreatedAt"), LocalDateTime.of(endDate, LocalTime.MAX)));
            }

            // query.orderBy(criteriaBuilder.desc(root.get("inquiryCreatedAt"))); // Pageable에 의해 처리

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Qna> qnaPage = qnaRepository.findAll(spec, pageable);

        return qnaPage.map(qna -> QnaSummaryResponseDTO.builder()
                .inquiryId(qna.getInquiryId())
                .inquiryTitle(qna.getInquiryTitle())
                .authorNickname(qna.getUser().getUserNickName())
                .inquiryStatus(qna.getInquiryStatus().name())
                .inquiryCreatedAt(qna.getInquiryCreatedAt())
                .hasAttachments(qna.getAttachments() != null && !qna.getAttachments().isEmpty())
                .build());
    }

    @Override
    public QnaDetailResponseDTO getMyQnaDetail(Integer qnaId, String userId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("문의를 찾을 수 없습니다: ID " + qnaId));

        // 본인 문의 여부 확인
        if (!qna.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("해당 문의에 접근할 권한이 없습니다.");
        }

        List<QnaAttachmentResponseDTO> attachmentDTOs = qna.getAttachments().stream()
                .map(att -> QnaAttachmentResponseDTO.builder()
                        .imgId(att.getImgId())
                        .fileUrl(att.getImgFilePath()) // 이미 전체 URL이 저장되어 있다고 가정
                        .originalFilename(att.getImgOriginalFilename())
                        .createdAt(att.getImgCreatedAt())
                        .build())
                .collect(Collectors.toList());

        QnaReplyResponseDTO replyDTO = null;
        if (qna.getQnaReply() != null) {
            replyDTO = QnaReplyResponseDTO.builder()
                    .replyId(qna.getQnaReply().getReplyId())
                    .replyContent(qna.getQnaReply().getReplyContent())
                    .replierNickname(qna.getQnaReply().getUser().getUserNickName()) // 답변자는 관리자
                    .replyCreatedAt(qna.getQnaReply().getReplyCreatedAt())
                    .replyUpdatedAt(qna.getQnaReply().getReplyUpdatedAt())
                    .build();
        }

        return QnaDetailResponseDTO.builder()
                .inquiryId(qna.getInquiryId())
                .inquiryTitle(qna.getInquiryTitle())
                .inquiryContent(qna.getInquiryContent())
                .authorNickname(qna.getUser().getUserNickName())
                .inquiryStatus(qna.getInquiryStatus().name())
                .inquiryCreatedAt(qna.getInquiryCreatedAt())
                .inquiryUpdatedAt(qna.getInquiryUpdatedAt())
                .attachments(attachmentDTOs)
                .reply(replyDTO)
                .build();
    }

    // S3 URL에서 객체 키를 추출하는 헬퍼 메서드 (필요시 사용)
    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        try {
            // "https://<bucket-name>.s3.<region>.amazonaws.com/<key>"
            // 또는 "https://s3.<region>.amazonaws.com/<bucket-name>/<key>" (가상 호스팅 스타일 vs 경로 스타일)
            // 가장 간단하게는 마지막 '/' 이후의 문자열을 키로 가정하거나, 버킷 이름 이후의 경로를 사용합니다.
            // S3FileStorageService에서 URL 생성 시 일관된 패턴을 사용했다면 파싱이 용이합니다.
            if (fileUrl.contains(QNA_FILE_SUBDIRECTORY + "/")) {
                return fileUrl.substring(fileUrl.indexOf(QNA_FILE_SUBDIRECTORY + "/"));
            }
            // 더 견고한 파싱 로직이 필요할 수 있음
            return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        } catch (Exception e) {
            log.warn("Could not extract key from URL: {}", fileUrl, e);
            return fileUrl; // 파싱 실패 시 원본 URL (또는 null) 반환
        }
    }

    // --- 관리자 QnA 메서드 구현 (추가) ---

    @Override
    public Page<AdminQnaSummaryResponseDTO> getAllQnasForAdmin(Pageable pageable, String searchTerm,
                                                               String statusFilter, LocalDate startDate, LocalDate endDate) {
        log.info("Admin: Fetching all QnAs. Page: {}, Size: {}, Search: '{}', Status: '{}', StartDate: {}, EndDate: {}",
                pageable.getPageNumber(), pageable.getPageSize(), searchTerm, statusFilter, startDate, endDate);

        Specification<Qna> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 검색어 조건
            if (StringUtils.hasText(searchTerm)) {
                String likePattern = "%" + searchTerm + "%"; // 원본 검색어 패턴 (CLOB 용)
                String lowerSearchTermPattern = "%" + searchTerm.toLowerCase() + "%"; // 소문자 변환 검색어 패턴 (VARCHAR 용)

                // Qna 엔티티의 User 필드를 통해 User 정보에 접근
                Join<Qna, User> userJoin = root.join("user", JoinType.INNER); // 명시적 조인

                Predicate titleMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("inquiryTitle")), // 제목 (VARCHAR)은 소문자 비교
                        lowerSearchTermPattern
                );
                Predicate contentMatch = criteriaBuilder.like( // 내용 (CLOB)은 LOWER() 없이 비교
                        root.get("inquiryContent"),
                        likePattern
                );
                Predicate userIdMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("userId")), // 작성자 ID (VARCHAR)는 소문자 비교
                        lowerSearchTermPattern
                );
                Predicate userNicknameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("userNickName")), // 작성자 닉네임 (VARCHAR)은 소문자 비교
                        lowerSearchTermPattern
                );
                predicates.add(criteriaBuilder.or(titleMatch, contentMatch, userIdMatch, userNicknameMatch));
            }

            // 답변 상태 필터
            if (StringUtils.hasText(statusFilter)) {
                try {
                    QnaStatus qnaStatus = QnaStatus.valueOf(statusFilter.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("inquiryStatus"), qnaStatus));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid QnaStatus filter value: {}", statusFilter);
                }
            }

            // 날짜 범위 필터 (작성일 기준)
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("inquiryCreatedAt"), LocalDateTime.of(startDate, LocalTime.MIN)));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("inquiryCreatedAt"), LocalDateTime.of(endDate, LocalTime.MAX)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Qna> qnaPage = qnaRepository.findAll(spec, pageable);

        return qnaPage.map(qna -> AdminQnaSummaryResponseDTO.builder()
                .inquiryId(qna.getInquiryId())
                .inquiryTitle(qna.getInquiryTitle())
                .authorUserId(qna.getUser() != null ? qna.getUser().getUserId() : "N/A")
                .authorNickname(qna.getUser() != null ? qna.getUser().getUserNickName() : "N/A")
                .inquiryStatus(qna.getInquiryStatus().name())
                .inquiryCreatedAt(qna.getInquiryCreatedAt())
                .reportCount(qna.getReports() != null ? qna.getReports().size() : 0)
                .hasAttachments(qna.getAttachments() != null && !qna.getAttachments().isEmpty())
                .build());
    }

    @Override
    public AdminQnaDetailResponseDTO getQnaDetailForAdmin(Integer qnaId) {
        // 1. 현재 요청을 보낸 관리자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentAdminUserId = null;
        User currentAdminUser = null;

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            currentAdminUserId = authentication.getName(); // Principal에서 사용자 ID (일반적으로 username) 가져오기
            if (currentAdminUserId != null) {
                // 사용자 ID로 User 엔티티 조회
                currentAdminUser = userRepository.findById(currentAdminUserId)
                        .orElse(null); // 사용자를 찾지 못할 경우 null
            }
        } else {
            log.warn("Admin QnA detail requested without proper authentication. Cannot determine current admin user.");
            // 인증 정보가 없으면 reportedByCurrentUserAdmin은 false로 유지됨
        }

        // 2. QnA 정보 조회
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("문의를 찾을 수 없습니다: ID " + qnaId));

        // 3. 첨부파일 DTO 목록 생성
        List<QnaAttachmentResponseDTO> attachmentDTOs = qna.getAttachments().stream()
                .map(att -> QnaAttachmentResponseDTO.builder()
                        .imgId(att.getImgId())
                        .fileUrl(att.getImgFilePath()) // S3 URL
                        .originalFilename(att.getImgOriginalFilename())
                        .createdAt(att.getImgCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // 4. 답변 DTO 생성
        QnaReplyResponseDTO replyDTO = null;
        if (qna.getQnaReply() != null) {
            replyDTO = QnaReplyResponseDTO.builder()
                    .replyId(qna.getQnaReply().getReplyId())
                    .replyContent(qna.getQnaReply().getReplyContent())
                    .replierNickname(qna.getQnaReply().getUser() != null ? qna.getQnaReply().getUser().getUserNickName() : "관리자")
                    .replyCreatedAt(qna.getQnaReply().getReplyCreatedAt())
                    .replyUpdatedAt(qna.getQnaReply().getReplyUpdatedAt())
                    .build();
        }

        // 5. 전체 신고 건수 계산
        long totalReportCount = qna.getReports() != null ? qna.getReports().size() : 0;

        // 6. 현재 로그인한 관리자가 이 QnA를 신고했는지 여부 확인
        boolean isReportedByThisAdmin = false;
        if (currentAdminUser != null) { // currentAdminUser가 null이 아닐 때만 (즉, 관리자 정보 조회가 성공했을 때만)
            // QnaReportRepository에 해당 메서드가 정의되어 있어야 함
            isReportedByThisAdmin = qnaReportRepository.existsByQnaAndUser(qna, currentAdminUser);
        }

        // 7. AdminQnaDetailResponseDTO 빌드하여 반환
        return AdminQnaDetailResponseDTO.builder()
                .inquiryId(qna.getInquiryId())
                .inquiryTitle(qna.getInquiryTitle())
                .inquiryContent(qna.getInquiryContent())
                .authorUserId(qna.getUser() != null ? qna.getUser().getUserId() : "N/A")
                .authorNickname(qna.getUser() != null ? qna.getUser().getUserNickName() : "N/A")
                .inquiryStatus(qna.getInquiryStatus().name())
                .inquiryCreatedAt(qna.getInquiryCreatedAt())
                .inquiryUpdatedAt(qna.getInquiryUpdatedAt())
                .attachments(attachmentDTOs)
                .reply(replyDTO)
                .reportCount(totalReportCount) // 전체 신고 건수
                .reportedByCurrentUserAdmin(isReportedByThisAdmin) // ⭐ 현재 관리자의 신고 여부 추가
                .build();
    }

    @Override
    @Transactional // 쓰기 작업
    public QnaReplyResponseDTO createReplyToQna(Integer qnaId, QnaReplyRequestDTO replyDTO, String adminUserId) {
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("답변할 문의를 찾을 수 없습니다: ID " + qnaId));

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new EntityNotFoundException("관리자 계정을 찾을 수 없습니다: " + adminUserId));

        // 이미 답변이 있는지 확인 (하나의 문의에 하나의 답변만 허용하는 경우)
        if (qna.getQnaReply() != null) {
            throw new IllegalStateException("이미 답변이 등록된 문의입니다."); // 또는 기존 답변을 수정하도록 유도
        }

        QnaReply newReply = QnaReply.builder()
                .qna(qna)
                .user(adminUser)
                .replyContent(replyDTO.getReplyContent())
                .build();
        QnaReply savedReply = qnaReplyRepository.save(newReply);

        // 문의 상태를 'ANSWERED'로 변경
        qna.setInquiryStatus(QnaStatus.ANSWERED);
        qna.setQnaReply(savedReply); // Qna 엔티티에도 답변 연관관계 설정
        qnaRepository.save(qna); // 변경된 상태 저장

        return QnaReplyResponseDTO.builder()
                .replyId(savedReply.getReplyId())
                .replyContent(savedReply.getReplyContent())
                .replierNickname(adminUser.getUserNickName())
                .replyCreatedAt(savedReply.getReplyCreatedAt())
                .replyUpdatedAt(savedReply.getReplyUpdatedAt())
                .build();
    }

    // --- 사용자 문의 수정/삭제 메서드 구현 (새로 추가) ---

    @Override
    @Transactional // 쓰기 작업
    public QnaDetailResponseDTO updateMyQna(Integer qnaId, QnaUpdateRequestDTO requestDTO, List<MultipartFile> newFiles, String userId) throws IOException {
        log.info("User {} updating QnA ID: {}", userId, qnaId);
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 문의를 찾을 수 없습니다: ID " + qnaId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        // 본인 문의 여부 확인
        if (!qna.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("해당 문의를 수정할 권한이 없습니다.");
        }

        // --- 👇 [수정] 답변 완료된 문의는 수정 불가 로직 추가 ---
        if (qna.getInquiryStatus() == QnaStatus.ANSWERED) {
            log.warn("User {} attempted to update an already answered QnA ID: {}. Denying update.", userId, qnaId);
            throw new IllegalStateException("이미 답변이 완료된 문의는 수정할 수 없습니다."); // 400 또는 409 에러로 처리될 수 있음 (GlobalExceptionHandler 설정에 따라)
        }
        // --- 👆 [수정] 답변 완료된 문의는 수정 불가 로직 추가 ---

        // 문의 제목 및 내용 업데이트
        qna.setInquiryTitle(requestDTO.getInquiryTitle());
        qna.setInquiryContent(requestDTO.getInquiryContent());

        // 기존 첨부파일 삭제 처리
        List<Integer> idsToDelete = requestDTO.getAttachmentIdsToDelete();
        List<QnaAttachment> remainingAttachments = new ArrayList<>();
        if (idsToDelete != null && !idsToDelete.isEmpty()) {
            List<QnaAttachment> attachmentsToRemove = new ArrayList<>();
            for (QnaAttachment attachment : qna.getAttachments()) {
                if (idsToDelete.contains(attachment.getImgId())) {
                    attachmentsToRemove.add(attachment);
                    fileStorageService.deleteFile(attachment.getImgFilePath());
                } else {
                    remainingAttachments.add(attachment);
                }
            }
            qnaAttachmentRepository.deleteAll(attachmentsToRemove);
            qna.getAttachments().removeAll(attachmentsToRemove);
        } else {
            remainingAttachments.addAll(qna.getAttachments());
        }

        List<QnaAttachmentResponseDTO> currentAttachmentDTOs = remainingAttachments.stream()
                .map(att -> QnaAttachmentResponseDTO.builder()
                        .imgId(att.getImgId())
                        .fileUrl(att.getImgFilePath())
                        .originalFilename(att.getImgOriginalFilename())
                        .createdAt(att.getImgCreatedAt())
                        .build())
                .collect(Collectors.toList());

        if (newFiles != null && !newFiles.isEmpty()) {
            List<String> uploadedFileUrls = fileStorageService.uploadFiles(newFiles, QNA_FILE_SUBDIRECTORY);
            for (int i = 0; i < newFiles.size(); i++) {
                MultipartFile file = newFiles.get(i);
                String fileUrl = uploadedFileUrls.get(i);

                QnaAttachment newAttachment = QnaAttachment.builder()
                        .qna(qna)
                        .imgFilePath(fileUrl)
                        .imgOriginalFilename(file.getOriginalFilename())
                        .imgSavedFilename(extractKeyFromUrl(fileUrl))
                        .build();
                qnaAttachmentRepository.save(newAttachment);
                qna.getAttachments().add(newAttachment);

                currentAttachmentDTOs.add(QnaAttachmentResponseDTO.builder()
                        .imgId(newAttachment.getImgId())
                        .fileUrl(newAttachment.getImgFilePath())
                        .originalFilename(newAttachment.getImgOriginalFilename())
                        .createdAt(newAttachment.getImgCreatedAt())
                        .build());
            }
        }

        Qna updatedQna = qnaRepository.save(qna);

        QnaReplyResponseDTO replyDTO = null;
        if (updatedQna.getQnaReply() != null) {
            replyDTO = QnaReplyResponseDTO.builder()
                    .replyId(updatedQna.getQnaReply().getReplyId())
                    .replyContent(updatedQna.getQnaReply().getReplyContent())
                    .replierNickname(updatedQna.getQnaReply().getUser().getUserNickName())
                    .replyCreatedAt(updatedQna.getQnaReply().getReplyCreatedAt())
                    .replyUpdatedAt(updatedQna.getQnaReply().getReplyUpdatedAt())
                    .build();
        }

        return QnaDetailResponseDTO.builder()
                .inquiryId(updatedQna.getInquiryId())
                .inquiryTitle(updatedQna.getInquiryTitle())
                .inquiryContent(updatedQna.getInquiryContent())
                .authorNickname(user.getUserNickName())
                .inquiryStatus(updatedQna.getInquiryStatus().name())
                .inquiryCreatedAt(updatedQna.getInquiryCreatedAt())
                .inquiryUpdatedAt(updatedQna.getInquiryUpdatedAt())
                .attachments(currentAttachmentDTOs)
                .reply(replyDTO)
                .build();
    }

    @Override
    @Transactional // 쓰기 작업
    public void deleteMyQna(Integer qnaId, String userId) {
        log.info("User {} deleting QnA ID: {}", userId, qnaId);
        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 문의를 찾을 수 없습니다: ID " + qnaId));

        // 본인 문의 여부 확인
        if (!qna.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("해당 문의를 삭제할 권한이 없습니다.");
        }

        // 1. S3에서 첨부파일 삭제
        if (qna.getAttachments() != null) {
            for (QnaAttachment attachment : qna.getAttachments()) {
                fileStorageService.deleteFile(attachment.getImgFilePath()); // 또는 getImgSavedFilename()
            }
        }
        // Qna 엔티티 삭제 시, QnaAttachment, QnaReply는 CascadeType.ALL 또는 CascadeType.REMOVE 등으로
        // 자동으로 함께 삭제되도록 설정되어 있다면 DB에서는 별도 삭제 호출이 필요 없을 수 있습니다.
        // (Qna 엔티티의 @OneToMany, @OneToOne 관계 설정 확인 필요)
        // 현재 Qna 엔티티에는 attachments와 reports에 cascade = CascadeType.ALL, orphanRemoval = true 설정,
        // qnaReply 에는 cascade = CascadeType.ALL, orphanRemoval = true 설정이 되어 있으므로,
        // qnaRepository.delete(qna) 호출 시 연관된 QnaAttachment, QnaReply, QnaReport 엔티티도 함께 삭제됩니다.

        // 만약 Cascade 설정이 없다면 수동으로 삭제:
        // if (qna.getQnaReply() != null) {
        //     qnaReplyRepository.delete(qna.getQnaReply());
        // }
        // qnaAttachmentRepository.deleteAll(qna.getAttachments());
        // qnaReportRepository.deleteAll(qna.getReports()); // QnaReport도 있다면

        qnaRepository.delete(qna); // Qna 삭제 (Cascade 설정에 따라 연관 엔티티도 삭제됨)
        log.info("QnA ID: {} deleted successfully by user {}", qnaId, userId);
    }

    // --- 관리자 답변 수정/삭제 메서드 구현 (새로 추가) ---

    @Override
    @Transactional // 쓰기 작업
    public QnaReplyResponseDTO updateAdminReply(Integer replyId, QnaReplyRequestDTO replyDTO, String adminUserId) {
        log.info("Admin {} updating reply ID: {}", adminUserId, replyId);

        QnaReply qnaReply = qnaReplyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 답변을 찾을 수 없습니다: ID " + replyId));

        // (선택 사항) 답변을 작성한 관리자 본인 또는 특정 권한을 가진 관리자만 수정 가능하도록 체크
        // if (!qnaReply.getUser().getUserId().equals(adminUserId)) {
        //     throw new AccessDeniedException("해당 답변을 수정할 권한이 없습니다.");
        // }
        // 현재는 요청한 adminUserId로 작성자 정보를 업데이트 하거나, 최초 작성자 정보를 유지할 수 있습니다.
        // 여기서는 내용만 업데이트하고, 작성자 정보는 최초 작성자를 유지하는 것으로 가정합니다.
        // 필요하다면, 답변 엔티티에 '최초 작성자', '최종 수정자' 필드를 둘 수도 있습니다.

        qnaReply.setReplyContent(replyDTO.getReplyContent());
        // qnaReply.setReplyUpdatedAt(LocalDateTime.now()); // @UpdateTimestamp 어노테이션이 자동으로 처리
        QnaReply updatedReply = qnaReplyRepository.save(qnaReply);

        // Qna 상태는 이미 'ANSWERED'일 것이므로 별도 변경은 필요 없을 수 있습니다.
        // 만약 수정 시에도 Qna의 updatedAt을 갱신하고 싶다면 qnaRepository.save(qnaReply.getQna()) 호출

        return QnaReplyResponseDTO.builder()
                .replyId(updatedReply.getReplyId())
                .replyContent(updatedReply.getReplyContent())
                .replierNickname(updatedReply.getUser().getUserNickName()) // 최초 작성자 닉네임
                .replyCreatedAt(updatedReply.getReplyCreatedAt())
                .replyUpdatedAt(updatedReply.getReplyUpdatedAt())
                .build();
    }

    @Override
    @Transactional // 쓰기 작업
    public void deleteAdminReply(Integer replyId, String adminUserId) {
        log.info("Admin {} deleting reply ID: {}", adminUserId, replyId);

        QnaReply qnaReply = qnaReplyRepository.findById(replyId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 답변을 찾을 수 없습니다: ID " + replyId));

        // (선택 사항) 답변을 작성한 관리자 본인 또는 특정 권한을 가진 관리자만 삭제 가능하도록 체크
        // if (!qnaReply.getUser().getUserId().equals(adminUserId)) {
        //    throw new AccessDeniedException("해당 답변을 삭제할 권한이 없습니다.");
        // }

        Qna qna = qnaReply.getQna();
        if (qna == null) {
            // 이론적으로 발생하기 어렵지만, 데이터 정합성 문제 방지
            throw new IllegalStateException("답변에 연결된 원본 문의를 찾을 수 없습니다.");
        }

        qnaReplyRepository.delete(qnaReply);

        // 답변이 삭제되었으므로 원본 문의(Qna)의 상태를 PENDING으로 변경
        qna.setInquiryStatus(QnaStatus.PENDING);
        qna.setQnaReply(null); // Qna 엔티티에서 답변 연관관계 제거
        qnaRepository.save(qna);

        log.info("Reply ID: {} deleted successfully. QnA ID: {} status updated to PENDING.", replyId, qna.getInquiryId());
    }

    // --- 관리자 QnA 신고 생성 메서드 구현 (새로 추가) ---

    @Override
    @Transactional // 쓰기 작업
    public QnaReportResponseDTO createQnaReportByAdmin(Integer qnaId, String adminUserId) {
        log.info("Admin {} attempting to create a report for QnA ID: {}", adminUserId, qnaId);

        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("신고할 문의를 찾을 수 없습니다: ID " + qnaId));

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new EntityNotFoundException("관리자 계정을 찾을 수 없습니다: " + adminUserId));

        // QnaReportRepository에 findByQnaAndUser 또는 existsByQnaAndUser 메서드가 정의되어 있다고 가정
        if (qnaReportRepository.existsByQnaAndUser(qna, adminUser)) { // existsBy... 사용 권장
            log.warn("Admin {} already reported QnA ID: {}. Throwing IllegalStateException.", adminUserId, qnaId);
            throw new IllegalStateException("이미 해당 관리자가 신고한 문의입니다."); // 409 Conflict 유도
        }

        QnaReport newReport = QnaReport.builder()
                .qna(qna)
                .user(adminUser)
                .build();
        QnaReport savedReport = qnaReportRepository.save(newReport);

        log.info("Admin {} successfully reported QnA ID: {}. New Report ID: {}", adminUserId, qnaId, savedReport.getInquiryReportId());
        return QnaReportResponseDTO.builder()
                .reportId(savedReport.getInquiryReportId())
                .qnaId(qnaId)
                .reporterUserId(adminUserId)
                .reportedAt(savedReport.getInquiryReportDate())
                .message("문의가 성공적으로 신고(접수)되었습니다.")
                .build();
    }

    // --- (관리자용) 신고된 QnA 목록 조회 메서드 구현 (새로 추가) ---
    @Override
    public Page<ReportedQnaItemResponseDTO> getReportedQnasForAdmin(
            Pageable pageable,
            String searchTerm,
            LocalDate reportStartDate,
            LocalDate reportEndDate) {

        log.info("Admin: Fetching reported QnAs. Page: {}, Size: {}, Search: '{}', ReportStart: {}, ReportEnd: {}",
                pageable.getPageNumber(), pageable.getPageSize(), searchTerm, reportStartDate, reportEndDate);

        Specification<Qna> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. QnaReport가 존재하는 QnA만 선택 (관리자가 신고한 QnA)
            // Subquery를 사용하여 QnaReport 테이블에 해당 QnA에 대한 레코드가 있는지 확인
            Subquery<Long> reportExistsSubquery = query.subquery(Long.class);
            Root<QnaReport> qnaReportRoot = reportExistsSubquery.from(QnaReport.class);
            reportExistsSubquery.select(criteriaBuilder.literal(1L)); // 존재 여부만 확인

            List<Predicate> subqueryPredicates = new ArrayList<>();
            subqueryPredicates.add(criteriaBuilder.equal(qnaReportRoot.get("qna"), root)); // QnaReport.qna == Qna (현재 Qna)

            // 2. 신고일(QnaReport.inquiryReportDate) 기준으로 필터링
            if (reportStartDate != null) {
                subqueryPredicates.add(criteriaBuilder.greaterThanOrEqualTo(qnaReportRoot.get("inquiryReportDate"), LocalDateTime.of(reportStartDate, LocalTime.MIN)));
            }
            if (reportEndDate != null) {
                subqueryPredicates.add(criteriaBuilder.lessThanOrEqualTo(qnaReportRoot.get("inquiryReportDate"), LocalDateTime.of(reportEndDate, LocalTime.MAX)));
            }
            reportExistsSubquery.where(criteriaBuilder.and(subqueryPredicates.toArray(new Predicate[0])));
            predicates.add(criteriaBuilder.exists(reportExistsSubquery));


            // 3. 검색어(searchTerm) 조건 (QnA 제목, 내용, QnA 작성자 ID, QnA 작성자 닉네임)
            if (StringUtils.hasText(searchTerm)) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("inquiryTitle")), likePattern);
                Predicate contentMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("inquiryContent")), likePattern);
                // User 엔티티 조인 (이미 ManyToOne으로 매핑되어 있음)
                Join<Qna, User> userJoin = root.join("user", JoinType.INNER);
                Predicate authorIdMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("userId")), likePattern);
                Predicate authorNicknameMatch = criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("userNickName")), likePattern);
                predicates.add(criteriaBuilder.or(titleMatch, contentMatch, authorIdMatch, authorNicknameMatch));
            }

            // 중복된 QnA가 결과에 포함되지 않도록 distinct 설정
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Qna> reportedQnaPage = qnaRepository.findAll(spec, pageable);

        return reportedQnaPage.map(qna -> ReportedQnaItemResponseDTO.builder()
                .id(qna.getInquiryId())
                .itemType("QNA") // 프론트엔드 구분용
                .authorId(qna.getUser() != null ? qna.getUser().getUserId() : "N/A")
                .authorNickname(qna.getUser() != null ? qna.getUser().getUserNickName() : "N/A")
                .titleOrContentSnippet(qna.getInquiryTitle())
                .originalPostDate(qna.getInquiryCreatedAt())
                .reportCount(qna.getReports() != null ? qna.getReports().size() : 0) // Qna에 연결된 전체 신고 수
                .build());
    }


}