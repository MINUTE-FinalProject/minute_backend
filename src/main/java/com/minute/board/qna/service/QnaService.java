package com.minute.board.qna.service;

import com.minute.board.qna.dto.request.QnaCreateRequestDTO;
import com.minute.board.qna.dto.request.QnaUpdateRequestDTO;
import com.minute.board.qna.dto.response.QnaDetailResponseDTO;
import com.minute.board.qna.dto.response.QnaSummaryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import com.minute.board.qna.dto.request.QnaReplyRequestDTO; // 추가
import com.minute.board.qna.dto.response.AdminQnaDetailResponseDTO; // 추가
import com.minute.board.qna.dto.response.AdminQnaSummaryResponseDTO; // 추가
import com.minute.board.qna.dto.response.QnaReplyResponseDTO; // 추가
import com.minute.board.qna.dto.response.QnaReportResponseDTO; // 추가
import jakarta.persistence.EntityNotFoundException;
import com.minute.board.qna.dto.response.ReportedQnaItemResponseDTO; // 추가

import java.io.IOException;
import java.time.LocalDate; // 추가
import java.util.List;

public interface QnaService {

    /**
     * 새로운 문의를 생성합니다 (첨부파일 포함).
     *
     * @param requestDTO 문의 생성 정보 DTO
     * @param files      첨부파일 목록
     * @param userId     작성자 ID (인증된 사용자)
     * @return 생성된 문의의 상세 정보 DTO
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    QnaDetailResponseDTO createQna(QnaCreateRequestDTO requestDTO, List<MultipartFile> files, String userId) throws IOException;

    /**
     * 현재 로그인한 사용자의 문의 목록을 검색어와 함께 페이징하여 조회합니다.
     *
     * @param userId     사용자 ID
     * @param pageable   페이징 정보
     * @param searchTerm 검색어 (제목 또는 내용)
     * @return 페이징된 문의 요약 정보 DTO 목록
     */
    Page<QnaSummaryResponseDTO> getMyQnas(String userId, Pageable pageable, String searchTerm);

    /**
     * 현재 로그인한 사용자의 특정 문의 상세 정보를 조회합니다.
     *
     * @param qnaId  조회할 문의 ID
     * @param userId 사용자 ID
     * @return 문의 상세 정보 DTO
     */
    QnaDetailResponseDTO getMyQnaDetail(Integer qnaId, String userId);

    // 여기에 나중에 문의 수정, 삭제 등의 메서드 시그니처가 추가될 것입니다.

    // --- 관리자 QnA 메서드 (추가) ---

    /**
     * (관리자용) 모든 문의 목록을 검색 및 필터 조건과 함께 페이징하여 조회합니다.
     *
     * @param pageable      페이징 정보
     * @param searchTerm    검색어 (제목, 내용, 작성자ID, 작성자닉네임 등)
     * @param statusFilter  답변 상태 필터 (PENDING, ANSWERED, null일 경우 전체)
     * @param startDate     검색 시작일 (작성일 기준)
     * @param endDate       검색 종료일 (작성일 기준)
     * @return 페이징된 관리자용 문의 요약 정보 DTO 목록
     */
    Page<AdminQnaSummaryResponseDTO> getAllQnasForAdmin(Pageable pageable, String searchTerm, String statusFilter, LocalDate startDate, LocalDate endDate);

    /**
     * (관리자용) 특정 문의의 상세 정보를 조회합니다.
     *
     * @param qnaId 조회할 문의 ID
     * @return 관리자용 문의 상세 정보 DTO
     */
    AdminQnaDetailResponseDTO getQnaDetailForAdmin(Integer qnaId);

    /**
     * (관리자용) 특정 문의에 답변을 작성합니다.
     *
     * @param qnaId       답변할 문의 ID
     * @param replyDTO    답변 내용 DTO
     * @param adminUserId 답변 작성 관리자 ID
     * @return 생성된 답변 정보 DTO
     */
    QnaReplyResponseDTO createReplyToQna(Integer qnaId, QnaReplyRequestDTO replyDTO, String adminUserId);

    // 여기에 나중에 관리자 답변 수정/삭제, 문의 강제 삭제 등의 메서드 시그니처가 추가될 것입니다.

    // --- 사용자 문의 수정/삭제 메서드 (새로 추가) ---

    /**
     * 현재 로그인한 사용자가 작성한 문의를 수정합니다.
     * (제목, 내용 수정 및 첨부파일 변경 - 기존 파일 삭제, 새 파일 추가)
     *
     * @param qnaId       수정할 문의 ID
     * @param requestDTO  수정할 내용 DTO (삭제할 첨부파일 ID 목록 포함)
     * @param newFiles    새로 첨부할 파일 목록
     * @param userId      요청 사용자 ID
     * @return 수정된 문의의 상세 정보 DTO
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    QnaDetailResponseDTO updateMyQna(Integer qnaId, QnaUpdateRequestDTO requestDTO, List<MultipartFile> newFiles, String userId) throws IOException;

    /**
     * 현재 로그인한 사용자가 작성한 문의를 삭제합니다.
     * (문의글, 관련 첨부파일(S3 및 DB), 관련 답변 모두 삭제)
     *
     * @param qnaId  삭제할 문의 ID
     * @param userId 요청 사용자 ID
     */
    void deleteMyQna(Integer qnaId, String userId);

    // 여기에 나중에 관리자 답변 수정/삭제, 문의 강제 삭제 등의 메서드 시그니처가 추가될 것입니다.

    // --- 관리자 답변 수정/삭제 메서드 (새로 추가) ---

    /**
     * (관리자용) 특정 문의 답변을 수정합니다.
     *
     * @param replyId     수정할 답변 ID
     * @param replyDTO    수정할 답변 내용 DTO
     * @param adminUserId 요청 관리자 ID
     * @return 수정된 답변 정보 DTO
     */
    QnaReplyResponseDTO updateAdminReply(Integer replyId, QnaReplyRequestDTO replyDTO, String adminUserId);

    /**
     * (관리자용) 특정 문의 답변을 삭제합니다.
     * 답변 삭제 시 해당 문의(Qna)의 상태는 'PENDING'으로 변경됩니다.
     *
     * @param replyId     삭제할 답변 ID
     * @param adminUserId 요청 관리자 ID
     */
    void deleteAdminReply(Integer replyId, String adminUserId);

    // 여기에 나중에 관리자 문의 강제 삭제 등의 메서드 시그니처가 추가될 것입니다.

    // --- 관리자 QnA 신고 생성 메서드 (새로 추가) ---
    /**
     * (관리자용) 특정 문의(QnA)에 대해 관리자가 신고(QnaReport 생성)합니다.
     * 이미 해당 관리자가 해당 문의를 신고한 경우, 기존 신고 정보를 반환하거나 적절한 메시지를 포함한 DTO를 반환합니다.
     *
     * @param qnaId       신고할 문의 ID
     * @param adminUserId 신고하는 관리자 ID
     * @return 생성된 QnaReport 정보 또는 관련 메시지를 담은 DTO
     * @throws EntityNotFoundException QnA 또는 관리자 User를 찾을 수 없는 경우
     */
    QnaReportResponseDTO createQnaReportByAdmin(Integer qnaId, String adminUserId);

    // --- (관리자용) 신고된 QnA 목록 조회 메서드 (새로 추가) ---
    /**
     * (관리자용) 신고된 QnA 목록을 조회합니다.
     * QnA는 관리자만 신고할 수 있으므로, 이 목록은 관리자에 의해 신고된 QnA 목록입니다.
     *
     * @param pageable        페이징 정보
     * @param searchTerm      검색어 (QnA 제목, 내용, QnA 작성자 ID/닉네임 대상)
     * @param reportStartDate 신고일 검색 시작일 (QnaReport의 inquiryReportDate 기준)
     * @param reportEndDate   신고일 검색 종료일 (QnaReport의 inquiryReportDate 기준)
     * @return 페이징된 신고된 QnA 아이템 DTO 목록
     */
    Page<ReportedQnaItemResponseDTO> getReportedQnasForAdmin(
            Pageable pageable,
            String searchTerm,
            LocalDate reportStartDate,
            LocalDate reportEndDate
    );
}