package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.common.dto.response.ReportSuccessResponseDTO;
import com.minute.board.free.dto.request.*; // request DTO들 import
import com.minute.board.free.dto.response.AdminReportedCommentEntryDTO;
import com.minute.board.free.dto.response.CommentLikeResponseDTO;
import com.minute.board.free.dto.response.FreeboardCommentResponseDTO;
// import com.minute.board.free.dto.response.ReportedCommentEntryDTO; // ReportedCommentEntryDTO는 현재 이 인터페이스에서 직접 사용 안됨
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public interface FreeboardCommentService {

    /**
     * 특정 게시글에 달린 댓글 목록을 페이징하여 조회합니다.
     *
     * @param postId 게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징 처리된 댓글 목록 (PageResponseDTO)
     */
    PageResponseDTO<FreeboardCommentResponseDTO> getCommentsByPostId(Integer postId, Pageable pageable);

    /**
     * 특정 게시글에 새로운 댓글을 작성합니다.
     *
     * @param postId 댓글을 작성할 게시글의 ID
     * @param requestDto 댓글 생성 요청 정보 (내용). 작성자 ID는 currentUserId로 전달받습니다.
     * @param currentUserId 현재 인증된 사용자의 ID
     * @return 생성된 댓글 정보 (FreeboardCommentResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글 또는 currentUserId에 해당하는 사용자가 없을 경우
     */
    FreeboardCommentResponseDTO createComment(Integer postId, FreeboardCommentRequestDTO requestDto, String currentUserId); // <<< currentUserId 파라미터 추가

    /**
     * 특정 ID의 댓글을 수정합니다.
     *
     * @param commentId 수정할 댓글의 ID
     * @param requestDto 수정할 내용 (내용).
     * @param currentUserId 현재 인증된 사용자의 ID (수정 권한 확인용)
     * @return 수정된 댓글 정보 (FreeboardCommentResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 댓글을 찾을 수 없거나, currentUserId에 해당하는 사용자가 없을 경우
     * @throws org.springframework.security.access.AccessDeniedException 댓글 수정 권한이 없을 경우
     */
    FreeboardCommentResponseDTO updateComment(Integer commentId, FreeboardCommentRequestDTO requestDto, String currentUserId); // <<< currentUserId 파라미터 추가

    /**
     * 특정 ID의 댓글을 삭제합니다.
     *
     * @param commentId 삭제할 댓글의 ID
     * @param currentUserId 삭제를 요청하는 현재 인증된 사용자의 ID (삭제 권한 확인용)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 댓글을 찾을 수 없을 경우
     * @throws org.springframework.security.access.AccessDeniedException 댓글 삭제 권한이 없을 경우
     */
    void deleteComment(Integer commentId, String currentUserId); // <<< requestUserId를 currentUserId로 변경

    /**
     * 특정 댓글에 대한 사용자의 좋아요 상태를 토글(추가/삭제)합니다.
     *
     * @param commentId 댓글 ID
     * @param currentUserId 좋아요를 누르는 현재 인증된 사용자의 ID
     * @return 댓글의 현재 좋아요 수와 사용자의 좋아요 상태 (CommentLikeResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 댓글 또는 사용자가 없을 경우
     */
    CommentLikeResponseDTO toggleCommentLike(Integer commentId, String currentUserId); // <<< requestDto 파라미터 제거, currentUserId 추가

    /**
     * 특정 댓글을 신고합니다. 사용자는 하나의 댓글에 대해 한 번만 신고할 수 있습니다.
     * 자신의 댓글은 신고할 수 없습니다.
     *
     * @param commentId 신고할 댓글 ID
     * @param currentUserId 신고하는 현재 인증된 사용자의 ID
     * @return 신고 처리 결과 메시지 (ReportSuccessResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 댓글 또는 사용자가 없을 경우
     * @throws IllegalStateException 이미 신고한 댓글이거나 자신의 댓글을 신고하려는 경우
     */
    ReportSuccessResponseDTO reportComment(Integer commentId, String currentUserId); // <<< requestDto 파라미터 제거, currentUserId 추가

    /**
     * 관리자가 신고된 댓글 목록을 페이징하여 조회합니다.
     *
     * @param filter 필터링 조건 DTO (AdminReportedCommentFilterDTO 또는 통일된 AdminReportFilterDTO 사용)
     * @param pageable 페이징 정보
     * @return 페이징된 신고 댓글 정보 목록 (PageResponseDTO)
     */
    PageResponseDTO<AdminReportedCommentEntryDTO> getReportedComments(AdminReportedCommentFilterDTO filter, Pageable pageable);
    // 주석에 AdminReportFilterDTO를 사용하도록 수정 제안이 있었으나, 현재 메소드 시그니처는 AdminReportedCommentFilterDTO를 사용하고 있습니다.
    // 만약 DTO 이름을 AdminReportFilterDTO로 통일하셨다면 위 파라미터 타입을 변경해주세요. 여기서는 일단 제공해주신 코드를 기준으로 합니다.

    /**
     * [관리자] 특정 댓글의 공개/숨김 상태를 변경합니다.
     *
     * @param commentId 대상 댓글 ID
     * @param requestDto 변경할 상태 정보 (isHidden)
     * @return 업데이트된 댓글 상세 정보 (FreeboardCommentResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 댓글이 없을 경우
     */
    FreeboardCommentResponseDTO updateCommentVisibility(Integer commentId, CommentVisibilityRequestDTO requestDto);

    /**
     * 특정 사용자가 작성한 댓글 목록을 검색/필터링하여 페이징 조회합니다.
     * (주로 "내가 쓴 댓글" 기능에 사용될 수 있습니다.)
     *
     * @param currentUserId 조회할 현재 인증된 사용자의 ID
     * @param filter 검색/필터 조건 DTO (AdminMyCommentFilterDTO 사용)
     * @param pageable 페이징 정보
     * @return 페이징된 해당 사용자의 댓글 목록
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 사용자가 없을 경우
     */
    PageResponseDTO<FreeboardCommentResponseDTO> getCommentsByAuthor(String currentUserId, @Nullable AdminMyCommentFilterDTO filter, Pageable pageable); // <<< userId를 currentUserId로 변경

    /**
     * 주어진 댓글 ID가 댓글 목록의 몇 번째 페이지에 속하는지 계산합니다.
     * @param commentId 페이지를 찾을 댓글의 ID
     * @param size 한 페이지당 댓글 수
     * @return 1부터 시작하는 페이지 번호
     */
    int getCommentPageNumber(Integer commentId, int size);
}