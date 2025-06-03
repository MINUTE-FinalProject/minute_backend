package com.minute.board.qna.service;

import com.minute.board.qna.dto.request.QnaCreateRequestDTO;
import com.minute.board.qna.dto.response.QnaDetailResponseDTO;
import com.minute.board.qna.dto.response.QnaSummaryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
}