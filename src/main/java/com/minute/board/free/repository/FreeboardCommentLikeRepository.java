package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardCommentLike;
import com.minute.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Query import 추가
import org.springframework.data.repository.query.Param; // Param import 추가

import java.util.List; // List import 추가
import java.util.Optional;
import java.util.Set;    // Set import 추가

public interface FreeboardCommentLikeRepository extends JpaRepository<FreeboardCommentLike, Integer> {
    // FreeboardCommentLike 엔티티의 ID (commentLikeId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 댓글로 좋아요 정보를 찾는 메서드
    Optional<FreeboardCommentLike> findByUserAndFreeboardComment(User user, FreeboardComment freeboardComment);

    // <<< 추가된 메소드 (N+1 해결용) >>>
    /**
     * 특정 사용자가 주어진 댓글 ID 목록 중에서 좋아요를 누른 댓글 ID들을 조회합니다.
     * @param userId 사용자 ID
     * @param commentIds 댓글 ID 목록
     * @return 좋아요를 누른 댓글 ID의 Set
     */
    @Query("SELECT fcl.freeboardComment.commentId FROM FreeboardCommentLike fcl WHERE fcl.user.userId = :userId AND fcl.freeboardComment.commentId IN :commentIds")
    Set<Integer> findLikedCommentIdsByUserIdAndCommentIdsIn(@Param("userId") String userId, @Param("commentIds") List<Integer> commentIds);
}