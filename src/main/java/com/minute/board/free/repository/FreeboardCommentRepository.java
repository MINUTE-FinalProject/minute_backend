package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph; // EntityGraph 사용 시
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreeboardCommentRepository extends JpaRepository<FreeboardComment, Integer> {

    /**
     * 특정 게시글 ID에 해당하는 댓글 목록을 페이징하여 조회합니다.
     * 댓글 작성자(user) 정보도 함께 조회하여 N+1 문제를 방지합니다.
     *
     * @param postId 게시글 ID
     * @param pageable 페이징 정보
     * @return 페이징된 댓글 목록
     */
    @EntityGraph(attributePaths = {"user"}) // 댓글 조회 시 작성자 정보(User)를 함께 fetch join
    Page<FreeboardComment> findByFreeboardPost_PostId(Integer postId, Pageable pageable);
    // FreeboardComment 엔티티에 FreeboardPost freeboardPost 필드가 있고,
    // FreeboardPost 엔티티에 Integer postId 필드가 있다고 가정합니다.
}