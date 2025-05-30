package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.entity.FreeboardPostLike;
import com.minute.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set; // Set import 추가

public interface FreeboardPostLikeRepository extends JpaRepository<FreeboardPostLike, Integer> {
    // FreeboardPostLike 엔티티의 ID (postLikeId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // 사용자와 게시글로 좋아요 정보를 찾는 메서드
    Optional<FreeboardPostLike> findByUserAndFreeboardPost(User user, FreeboardPost freeboardPost);

    // <<< 추가된 메소드 >>>
    // 사용자와 게시글로 좋아요 존재 여부를 확인하는 메서드 (boolean 반환)
    boolean existsByUserAndFreeboardPost(User user, FreeboardPost freeboardPost);

    // <<< 추가된 메소드 (N+1 해결용) >>>
    /**
     * 특정 사용자가 주어진 게시글 ID 목록 중에서 좋아요를 누른 게시글 ID들을 조회합니다.
     * @param userId 사용자 ID
     * @param postIds 게시글 ID 목록
     * @return 좋아요를 누른 게시글 ID의 Set
     */
    @Query("SELECT fpl.freeboardPost.postId FROM FreeboardPostLike fpl WHERE fpl.user.userId = :userId AND fpl.freeboardPost.postId IN :postIds")
    Set<Integer> findLikedPostIdsByUserIdAndPostIdsIn(@Param("userId") String userId, @Param("postIds") List<Integer> postIds);

    // 특정 게시글의 모든 좋아요 삭제 (게시글 삭제 시 사용될 수 있으나, CASCADE로 처리 중이면 불필요)
    // void deleteByFreeboardPost(FreeboardPost freeboardPost);

    // 특정 사용자의 모든 좋아요 삭제 (사용자 탈퇴 시 사용될 수 있음)
    // void deleteByUser(User user);
}