package com.minute.board.qna.service;

import com.minute.board.qna.dto.request.QnaCreateRequestDTO;
import com.minute.board.qna.dto.response.QnaDetailResponseDTO;
import com.minute.board.qna.dto.response.QnaSummaryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import com.minute.board.qna.dto.request.QnaReplyRequestDTO; // 추가
import com.minute.board.qna.dto.response.AdminQnaDetailResponseDTO; // 추가
import com.minute.board.qna.dto.response.AdminQnaSummaryResponseDTO; // 추가
import com.minute.board.qna.dto.response.QnaReplyResponseDTO; // 추가

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
}