package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardPost;
import com.minute.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreeboardPostRepository extends JpaRepository<FreeboardPost, Integer> {
    // FreeboardPost 엔티티의 ID (postId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // FreeboardPostRepository.java 에 추가
// @EntityGraph(attributePaths = {"user"}) // LAZY 로딩인 user 필드를 EAGER 로딩처럼 함께 조회
// Page<FreeboardPost> findAll(Pageable pageable); // 기존 findAll도 EntityGraph 적용 가능

// 또는 JPQL 사용
// @Query("SELECT fp FROM FreeboardPost fp JOIN FETCH fp.user")
// Page<FreeboardPost> findAllWithUser(Pageable pageable);

    List<FreeboardPost> findByUserOrderByPostCreatedAtDesc(User user);
}