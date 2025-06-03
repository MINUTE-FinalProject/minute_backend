package com.minute.board.qna.service.implement;

import com.minute.board.qna.dto.request.QnaCreateRequestDTO;
import com.minute.board.qna.dto.response.QnaAttachmentResponseDTO;
import com.minute.board.qna.dto.response.QnaDetailResponseDTO;
import com.minute.board.qna.dto.response.QnaReplyResponseDTO;
import com.minute.board.qna.dto.response.QnaSummaryResponseDTO;
import com.minute.board.qna.entity.Qna;
import com.minute.board.qna.entity.QnaAttachment;
import com.minute.board.qna.entity.QnaStatus;
import com.minute.board.qna.repository.QnaAttachmentRepository;
import com.minute.board.qna.repository.QnaRepository;
import com.minute.board.qna.service.QnaService;
import com.minute.common.file.service.FileStorageService; // FileStorageService 인터페이스
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException; // 권한 예외
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // StringUtils 임포트
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션, 쓰기 작업 메서드에 @Transactional 추가
public class QnaServiceImpl implements QnaService {

    private final QnaRepository qnaRepository;
    private final QnaAttachmentRepository qnaAttachmentRepository;
    private final UserRepository userRepository;

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
    public Page<QnaSummaryResponseDTO> getMyQnas(String userId, Pageable pageable, String searchTerm) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        Page<Qna> qnaPage;
        if (StringUtils.hasText(searchTerm)) {
            // QnaRepository에 새로 추가한 검색 메서드 사용
            qnaPage = qnaRepository.findByUser_UserIdAndSearchTermOrderByInquiryCreatedAtDesc(userId, searchTerm, pageable);
        } else {
            qnaPage = qnaRepository.findByUser_UserIdOrderByInquiryCreatedAtDesc(userId, pageable);
        }

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
}