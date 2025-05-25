package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.free.dto.request.FreeboardPostRequestDTO;
import com.minute.board.free.dto.response.FreeboardPostResponseDTO;
import com.minute.board.free.dto.response.FreeboardPostSimpleResponseDTO;
import org.springframework.data.domain.Pageable;

public interface FreeboardPostService {

    /**
     * 모든 자유게시판 게시글 목록을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기, 정렬 조건)
     * @return 페이징 처리된 게시글 목록 (PageResponseDTO)
     */
    PageResponseDTO<FreeboardPostSimpleResponseDTO> getAllPosts(Pageable pageable);

    // 여기에 다른 게시글 관련 서비스 메서드들이 추가될 예정입니다.
    // 예: getPostById(Long postId), createPost(FreeboardPostRequestDTO requestDto, String userId), ...

    /**
     * 특정 ID의 자유게시판 게시글 상세 정보를 조회합니다.
     * 조회 시 해당 게시글의 조회수가 1 증가합니다.
     *
     * @param postId 조회할 게시글의 ID
     * @return 게시글 상세 정보 (FreeboardPostResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글이 없을 경우
     */
    FreeboardPostResponseDTO getPostById(Integer postId); // postId의 타입은 엔티티와 일치 (Integer)

    /**
     * 새로운 자유게시판 게시글을 생성합니다.
     *
     * @param requestDto 게시글 생성 요청 정보 (제목, 내용, 작성자 ID 등)
     * @return 생성된 게시글 상세 정보 (FreeboardPostResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 요청 DTO의 userId에 해당하는 사용자가 없을 경우
     */
    FreeboardPostResponseDTO createPost(FreeboardPostRequestDTO requestDto);
}

