package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardCommentLike;
import com.minute.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreeboardCommentLikeRepository extends JpaRepository<FreeboardCommentLike, Integer> {
    // FreeboardCommentLike 엔티티의 ID (commentLikeId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 댓글로 좋아요 정보를 찾는 메서드
    Optional<FreeboardCommentLike> findByUserAndFreeboardComment(User user, FreeboardComment freeboardComment);
}