package com.minute.board.free.repository;

import com.minute.board.free.entity.FreeboardComment;
import com.minute.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Sort import 추가
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable; // Spring의 @Nullable 사용

import java.time.LocalDateTime;
import java.util.List; // List import 추가

public interface FreeboardCommentRepository extends JpaRepository<FreeboardComment, Integer>, JpaSpecificationExecutor<FreeboardComment> {

    // --- 기존에 있던 유용한 메서드들 (절대 삭제하지 마세요!) ---
    @EntityGraph(attributePaths = {"user", "freeboardPost"}) // 댓글 목록 조회 시 N+1 방지
    Page<FreeboardComment> findByFreeboardPost_PostId(Integer postId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "freeboardPost"}) // "내가 쓴 댓글" 조회 시 N+1 방지
    Page<FreeboardComment> findByUserOrderByCommentCreatedAtDesc(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "freeboardPost"}) // N+1 방지
    List<FreeboardComment> findByUserOrderByCommentCreatedAtDesc(User user); // Pageable 없이 List 반환


    // --- JpaSpecificationExecutor의 메서드 오버라이드 및 @EntityGraph 적용 ---
    // (AdminReportViewServiceImpl에서 Specification과 Pageable을 함께 사용하는 findAll을 호출할 때 사용됨)
    @Override
    @EntityGraph(attributePaths = {
            "user", // 댓글 작성자
            "freeboardPost", // 댓글이 달린 원본 게시글
            "freeboardPost.user" // 원본 게시글의 작성자 (필요시)
            // 만약 FreeboardCommentReport를 직접 다루는 리포지토리라면, "report.user" (신고자) 등도 포함
    })
    Page<FreeboardComment> findAll(@Nullable Specification<FreeboardComment> spec, Pageable pageable);

    // (AdminReportViewServiceImpl에서 Specification과 Sort를 함께 사용하는 findAll을 호출할 때 사용됨)
    @Override
    @EntityGraph(attributePaths = {
            "user",
            "freeboardPost",
            "freeboardPost.user"
            // 만약 FreeboardCommentReport를 직접 다루는 리포지토리라면, "report.user" (신고자) 등도 포함
    })
    List<FreeboardComment> findAll(@Nullable Specification<FreeboardComment> spec, Sort sort);

    /**
     * 특정 게시글 내에서, 주어진 생성 시간보다 먼저 작성된 댓글의 개수를 반환합니다.
     * 댓글은 오래된 순(ASC)으로 정렬된다는 것을 전제로 합니다.
     * @param postId 게시글 ID
     * @param createdAt 기준이 되는 댓글의 생성 시간
     * @return 기준 시간 이전의 댓글 수
     */
    @Query("SELECT count(c) FROM FreeboardComment c WHERE c.freeboardPost.postId = :postId AND c.commentCreatedAt < :createdAt")
    long countPreviousComments(@Param("postId") Integer postId, @Param("createdAt") LocalDateTime createdAt);
}