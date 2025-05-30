package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.common.dto.response.ReportSuccessResponseDTO;
import com.minute.board.free.dto.request.*; // request DTO들 import
import com.minute.board.free.dto.response.*; // response DTO들 import
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable; // @Nullable 어노테이션 사용

import java.time.LocalDate;

public interface FreeboardPostService {

    /**
     * 자유게시판 게시글 목록을 페이징하여 조회합니다.
     * 다양한 필터 조건(작성자, 검색 키워드, 날짜 범위)을 적용할 수 있습니다.
     * 기본적으로 숨김 처리되지 않은 게시글만 조회합니다.
     *
     * @param pageable 페이징 정보
     * @param authorUserId 조회할 작성자의 ID (선택 사항)
     * @param searchKeyword 검색할 키워드 (선택 사항, 제목/내용/닉네임 통합 검색)
     * @param startDate 검색 시작일 (선택 사항)
     * @param endDate 검색 종료일 (선택 사항)
     * @return 페이징 처리된 게시글 목록 (PageResponseDTO)
     */
    PageResponseDTO<FreeboardPostSimpleResponseDTO> getAllPosts(
            Pageable pageable,
            @Nullable String authorUserId,
            @Nullable String searchKeyword,
            @Nullable LocalDate startDate,
            @Nullable LocalDate endDate
    );

    /**
     * 특정 ID의 자유게시판 게시글 상세 정보를 조회합니다.
     * 조회 시 해당 게시글의 조회수가 1 증가합니다.
     *
     * @param postId 조회할 게시글의 ID
     * @return 게시글 상세 정보 (FreeboardPostResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글이 없을 경우
     */
    FreeboardPostResponseDTO getPostById(Integer postId);

    /**
     * 새로운 자유게시판 게시글을 생성합니다.
     *
     * @param requestDto 게시글 생성 요청 정보 (제목, 내용). 작성자 ID는 currentUserId로 전달받습니다.
     * @param currentUserId 현재 인증된 사용자의 ID
     * @return 생성된 게시글 상세 정보 (FreeboardPostResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException currentUserId에 해당하는 사용자가 없을 경우
     */
    FreeboardPostResponseDTO createPost(FreeboardPostRequestDTO requestDto, String currentUserId); // <<< currentUserId 파라미터 추가

    /**
     * 특정 ID의 자유게시판 게시글을 수정합니다.
     *
     * @param postId 수정할 게시글의 ID
     * @param requestDto 수정할 내용 (제목, 내용).
     * @param currentUserId 현재 인증된 사용자의 ID (수정 권한 확인용)
     * @return 수정된 게시글 상세 정보 (FreeboardPostResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글이 없거나, currentUserId에 해당하는 사용자가 없을 경우
     * @throws org.springframework.security.access.AccessDeniedException 수정 권한이 없을 경우
     */
    FreeboardPostResponseDTO updatePost(Integer postId, FreeboardPostRequestDTO requestDto, String currentUserId); // <<< currentUserId 파라미터 추가

    /**
     * 특정 ID의 자유게시판 게시글을 삭제합니다.
     *
     * @param postId 삭제할 게시글의 ID
     * @param currentUserId 삭제를 요청하는 현재 인증된 사용자의 ID (삭제 권한 확인용)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글이 없을 경우
     * @throws org.springframework.security.access.AccessDeniedException 삭제 권한이 없을 경우
     */
    void deletePost(Integer postId, String currentUserId); // <<< requestUserId를 currentUserId로 변경

    /**
     * 특정 게시글에 대한 사용자의 좋아요 상태를 토글(추가/삭제)합니다.
     *
     * @param postId 게시글 ID
     * @param currentUserId 좋아요를 누르는 현재 인증된 사용자의 ID
     * @return 게시글의 현재 좋아요 수와 사용자의 좋아요 상태 (PostLikeResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글 또는 사용자가 없을 경우
     */
    PostLikeResponseDTO togglePostLike(Integer postId, String currentUserId); // <<< requestDto 파라미터 제거, currentUserId 추가

    /**
     * 특정 게시글을 신고합니다. 사용자는 하나의 게시글에 대해 한 번만 신고할 수 있습니다.
     * 자신의 게시글은 신고할 수 없습니다.
     *
     * @param postId 신고할 게시글 ID
     * @param currentUserId 신고하는 현재 인증된 사용자의 ID
     * @return 신고 처리 결과 메시지 (ReportSuccessResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글 또는 사용자가 없을 경우
     * @throws IllegalStateException 이미 신고한 게시글이거나 자신의 게시글을 신고하려는 경우
     */
    ReportSuccessResponseDTO reportPost(Integer postId, String currentUserId); // <<< requestDto 파라미터 제거, currentUserId 추가

    /**
     * 관리자가 신고된 게시글 목록을 페이징하여 조회합니다.
     *
     * @param filter 필터링 조건 DTO
     * @param pageable 페이징 정보
     * @return 페이징된 신고 게시글 정보 목록 (PageResponseDTO)
     */
    PageResponseDTO<ReportedPostEntryDTO> getReportedPosts(AdminReportedPostFilterDTO filter, Pageable pageable);

    /**
     * [관리자] 특정 게시글의 공개/숨김 상태를 변경합니다.
     *
     * @param postId 대상 게시글 ID
     * @param requestDto 변경할 상태 정보 (isHidden)
     * @return 업데이트된 게시글 상세 정보 (FreeboardPostResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글이 없을 경우
     */
    FreeboardPostResponseDTO updatePostVisibility(Integer postId, PostVisibilityRequestDTO requestDto);

    /**
     * 특정 사용자의 자유게시판 활동 내역(작성한 게시글 및 댓글)을 통합하여 최신순으로 페이징 조회합니다.
     *
     * @param currentUserId 조회할 현재 인증된 사용자의 ID
     * @param pageable 페이징 정보 (정렬 기준은 주로 createdAt)
     * @return 페이징된 사용자 활동 목록
     */
    PageResponseDTO<FreeboardUserActivityItemDTO> getUserFreeboardActivity(String currentUserId, Pageable pageable); // <<< userId를 currentUserId로 변경
}