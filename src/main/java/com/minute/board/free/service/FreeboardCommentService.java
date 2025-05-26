package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.common.dto.response.ReportSuccessResponseDTO;
import com.minute.board.free.dto.request.CommentLikeRequestDTO;
import com.minute.board.free.dto.request.CommentReportRequestDTO;
import com.minute.board.free.dto.request.FreeboardCommentRequestDTO;
import com.minute.board.free.dto.response.CommentLikeResponseDTO;
import com.minute.board.free.dto.response.FreeboardCommentResponseDTO;
import com.minute.board.free.dto.response.ReportedCommentEntryDTO;
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

    /**
     * 특정 게시글에 새로운 댓글을 작성합니다.
     *
     * @param postId 댓글을 작성할 게시글의 ID
     * @param requestDto 댓글 생성 요청 정보 (내용, 작성자 ID)
     * @return 생성된 댓글 정보 (FreeboardCommentResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 게시글 또는 사용자가 없을 경우
     */
    FreeboardCommentResponseDTO createComment(Integer postId, FreeboardCommentRequestDTO requestDto);

    /**
     * 특정 ID의 댓글을 수정합니다.
     *
     * @param commentId 수정할 댓글의 ID
     * @param requestDto 수정할 내용 (내용, 수정 요청자 ID)
     * @return 수정된 댓글 정보 (FreeboardCommentResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 댓글을 찾을 수 없거나, 요청 DTO의 userId에 해당하는 사용자가 없을 경우
     * @throws org.springframework.security.access.AccessDeniedException 댓글 수정 권한이 없을 경우 (임시 로직)
     */
    FreeboardCommentResponseDTO updateComment(Integer commentId, FreeboardCommentRequestDTO requestDto);

    /**
     * 특정 ID의 댓글을 삭제합니다.
     *
     * @param commentId 삭제할 댓글의 ID
     * @param requestUserId 삭제를 요청하는 사용자의 ID (임시 권한 확인용)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 댓글을 찾을 수 없을 경우
     * @throws org.springframework.security.access.AccessDeniedException 댓글 삭제 권한이 없을 경우 (임시 로직)
     */
    void deleteComment(Integer commentId, String requestUserId);

    /**
     * 특정 댓글에 대한 사용자의 좋아요 상태를 토글(추가/삭제)합니다.
     *
     * @param commentId 댓글 ID
     * @param requestDto 좋아요 요청 DTO (사용자 ID 포함)
     * @return 댓글의 현재 좋아요 수와 사용자의 좋아요 상태 (CommentLikeResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 댓글 또는 사용자가 없을 경우
     */
    CommentLikeResponseDTO toggleCommentLike(Integer commentId, CommentLikeRequestDTO requestDto);

    /**
     * 특정 댓글을 신고합니다. 사용자는 하나의 댓글에 대해 한 번만 신고할 수 있습니다.
     * 자신의 댓글은 신고할 수 없습니다.
     *
     * @param commentId 신고할 댓글 ID
     * @param requestDto 신고 요청 DTO (신고자 ID 포함)
     * @return 신고 처리 결과 메시지 (ReportSuccessResponseDTO)
     * @throws jakarta.persistence.EntityNotFoundException 해당 ID의 댓글 또는 사용자가 없을 경우
     * @throws IllegalStateException 이미 신고한 댓글이거나 자신의 댓글을 신고하려는 경우
     */
    ReportSuccessResponseDTO reportComment(Integer commentId, CommentReportRequestDTO requestDto);

    /**
     * 관리자가 신고된 댓글 목록을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 신고 댓글 정보 목록 (PageResponseDTO)
     */
    PageResponseDTO<ReportedCommentEntryDTO> getReportedComments(Pageable pageable);
}