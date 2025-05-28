package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardPost;
import com.minute.user.entity.User;
import io.micrometer.common.lang.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FreeboardPostRepository extends JpaRepository<FreeboardPost, Integer>, JpaSpecificationExecutor<FreeboardPost> {
    // FreeboardPost 엔티티의 ID (postId) 타입은 Integer 입니다.
    // 기능 구현 시 필요한 쿼리 메서드를 여기에 추가합니다.

    // FreeboardPostRepository.java 에 추가
// @EntityGraph(attributePaths = {"user"}) // LAZY 로딩인 user 필드를 EAGER 로딩처럼 함께 조회
// Page<FreeboardPost> findAll(Pageable pageable); // 기존 findAll도 EntityGraph 적용 가능

// 또는 JPQL 사용
// @Query("SELECT fp FROM FreeboardPost fp JOIN FETCH fp.user")
// Page<FreeboardPost> findAllWithUser(Pageable pageable);

    List<FreeboardPost> findByUserOrderByPostCreatedAtDesc(User user);

    /**
     * 모든 게시글을 페이징하여 조회 (사용자 정보 포함).
     * N+1 문제 방지를 위해 user 엔티티를 함께 fetch join 합니다.
     */
    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<FreeboardPost> findAll(Pageable pageable);

    /**
     * 특정 사용자가 작성한 게시글 목록을 페이징하여 조회 (사용자 정보 포함).
     * N+1 문제 방지를 위해 user 엔티티를 함께 fetch join 합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @param pageable 페이징 정보
     * @return 페이징된 해당 사용자의 게시글 목록
     */
    @EntityGraph(attributePaths = {"user"})
    Page<FreeboardPost> findByUser_UserId(String userId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<FreeboardPost> findAll(@Nullable Specification<FreeboardPost> spec, Pageable pageable);
}