package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.PageResponseDTO;
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
}