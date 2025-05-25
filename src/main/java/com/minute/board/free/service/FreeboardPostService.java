package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.free.dto.request.FreeboardPostRequestDTO;
import com.minute.board.free.dto.request.PostLikeRequestDTO;
import com.minute.board.free.dto.response.FreeboardPostResponseDTO;
import com.minute.board.free.dto.response.FreeboardPostSimpleResponseDTO;
import com.minute.board.free.dto.response.PostLikeResponseDTO;
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

    /**
     * 특정 ID의 자유게시판 게시글을 수정합니다.
     *
     * @param postId 수정할 게시글의 ID
     * @param requestDto 수정할 내용 (제목, 내용). 요청 DTO의 userId는 수정 권한 확인에 임시로 사용될 수 있습니다.
     * @return 수정된 게시글 상세 정보 (FreeboardPostResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글이 없거나, 요청 DTO의 userId에 해당하는 사용자가 없을 경우
     * @throws org.springframework.security.access.AccessDeniedException 수정 권한이 없을 경우 (임시 로직)
     */
    FreeboardPostResponseDTO updatePost(Integer postId, FreeboardPostRequestDTO requestDto);

    /**
     * 특정 ID의 자유게시판 게시글을 삭제합니다.
     *
     * @param postId 삭제할 게시글의 ID
     * @param requestUserId 삭제를 요청하는 사용자의 ID (임시 권한 확인용)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글이 없을 경우
     * @throws org.springframework.security.access.AccessDeniedException 삭제 권한이 없을 경우 (임시 로직)
     */
    void deletePost(Integer postId, String requestUserId); // 반환 타입 void 또는 간단한 성공 메시지 DTO 가능

    /**
     * 특정 게시글에 대한 사용자의 좋아요 상태를 토글(추가/삭제)합니다.
     *
     * @param postId 게시글 ID
     * @param requestDto 좋아요 요청 DTO (사용자 ID 포함)
     * @return 게시글의 현재 좋아요 수와 사용자의 좋아요 상태 (PostLikeResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글 또는 사용자가 없을 경우
     */
    PostLikeResponseDTO togglePostLike(Integer postId, PostLikeRequestDTO requestDto);
}

