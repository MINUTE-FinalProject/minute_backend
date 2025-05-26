package com.minute.board.free.repository;

import com.minute.board.free.dto.response.ReportedCommentEntryDTO;
import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardCommentReport;
import com.minute.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FreeboardCommentReportRepository extends JpaRepository<FreeboardCommentReport, Integer> {
    // FreeboardCommentReport 엔티티의 ID (commentReportId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 댓글로 이미 신고했는지 확인하는 메서드
    boolean existsByUserAndFreeboardComment(User user, FreeboardComment freeboardComment);

    /**
     * 신고된 댓글 목록과 각 댓글의 신고 횟수, 작성자 정보, 원본 게시글 ID 등을 페이징하여 조회합니다.
     * 신고 횟수가 많은 순으로 정렬합니다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 신고 댓글 정보 DTO 목록
     */
    @Query("SELECT new com.minute.board.free.dto.response.ReportedCommentEntryDTO(" +
            "c.commentId, c.commentContent, u.userId, u.userNickName, c.commentCreatedAt, p.postId, COUNT(r.commentReportId), c.commentIsHidden) " +
            "FROM FreeboardCommentReport r " +
            "JOIN r.freeboardComment c " +  // 신고된 댓글 정보 조인
            "JOIN c.user u " +             // 해당 댓글의 작성자 정보 조인
            "JOIN c.freeboardPost p " +    // 해당 댓글이 달린 게시글 정보 조인 (postId 가져오기 위함)
            "GROUP BY c.commentId, c.commentContent, u.userId, u.userNickName, c.commentCreatedAt, p.postId, c.commentIsHidden " +
            "ORDER BY COUNT(r.commentReportId) DESC, c.commentCreatedAt DESC") // 기본 정렬: 신고 많은 순, 다음은 최신 댓글 순
    Page<ReportedCommentEntryDTO> findReportedCommentSummaries(Pageable pageable);
}