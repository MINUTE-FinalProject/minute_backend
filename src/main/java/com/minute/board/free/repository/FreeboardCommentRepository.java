package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardComment;
import com.minute.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph; // EntityGraph 사용 시
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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

    List<FreeboardComment> findByUserOrderByCommentCreatedAtDesc(User user);
// 댓글 조회 시 원본 게시글 정보(originalPostTitle)를 효율적으로 가져오기 위해 @EntityGraph(attributePaths = {"freeboardPost", "freeboardPost.user"}) 등을 고려할 수 있습니다.
// 또는 findByUserOrderByCommentCreatedAtDesc 메서드에서 JOIN FETCH를 사용
// @Query("SELECT fc FROM FreeboardComment fc JOIN FETCH fc.freeboardPost p JOIN FETCH p.user WHERE fc.user = :user ORDER BY fc.commentCreatedAt DESC")
// List<FreeboardComment> findByUserWithPostAndPostAuthorOrderByCommentCreatedAtDesc(@Param("user") User user);

    /**
     * 특정 사용자가 작성한 댓글 목록을 페이징하여 조회합니다.
     * 댓글 작성자(user) 정보와 댓글이 달린 원본 게시글(freeboardPost) 정보도 함께 조회합니다.
     * 기본 정렬은 댓글 작성일 내림차순입니다.
     *
     * @param user 조회할 사용자 엔티티
     * @param pageable 페이징 정보
     * @return 페이징된 해당 사용자의 댓글 목록
     */
    @EntityGraph(attributePaths = {"user", "freeboardPost", "freeboardPost.user"}) // 댓글작성자, 원본게시글, 원본게시글작성자 함께 로딩
    Page<FreeboardComment> findByUserOrderByCommentCreatedAtDesc(User user, Pageable pageable);

}