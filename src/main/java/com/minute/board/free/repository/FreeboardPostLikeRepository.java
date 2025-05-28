package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.entity.FreeboardPostLike;
import com.minute.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreeboardPostLikeRepository extends JpaRepository<FreeboardPostLike, Integer> {
    // FreeboardPostLike 엔티티의 ID (postLikeId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 게시글로 좋아요 정보를 찾는 메서드
    Optional<FreeboardPostLike> findByUserAndFreeboardPost(User user, FreeboardPost freeboardPost);

    // <<< 추가된 메소드 >>>
    // 사용자와 게시글로 좋아요 존재 여부를 확인하는 메서드 (boolean 반환)
    boolean existsByUserAndFreeboardPost(User user, FreeboardPost freeboardPost);

    // 특정 게시글의 모든 좋아요 삭제 (게시글 삭제 시 사용될 수 있으나, CASCADE로 처리 중이면 불필요)
    // void deleteByFreeboardPost(FreeboardPost freeboardPost);

    // 특정 사용자의 모든 좋아요 삭제 (사용자 탈퇴 시 사용될 수 있음)
    // void deleteByUser(User user);
}