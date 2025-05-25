package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.free.dto.response.FreeboardCommentResponseDTO;
import org.springframework.data.domain.Pageable;

public interface FreeboardCommentService {

    /**
     * 특정 게시글에 달린 댓글 목록을 페이징하여 조회합니다.
     *
     * @param postId 게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징 처리된 댓글 목록 (PageResponseDTO)
     */
    PageResponseDTO<FreeboardCommentResponseDTO> getCommentsByPostId(Integer postId, Pageable pageable);

    // 여기에 댓글 작성, 수정, 삭제 등 다른 서비스 메서드들이 추가될 예정입니다.
}