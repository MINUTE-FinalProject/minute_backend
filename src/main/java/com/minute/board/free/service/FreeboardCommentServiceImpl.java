package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.free.dto.request.FreeboardCommentRequestDTO;
import com.minute.board.free.dto.response.FreeboardCommentResponseDTO;
import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.repository.FreeboardCommentRepository;
import com.minute.board.free.repository.FreeboardPostRepository;
import com.minute.user.entity.User; // User 엔티티 import
import com.minute.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreeboardCommentServiceImpl implements FreeboardCommentService {

    private final FreeboardCommentRepository freeboardCommentRepository;
    // private final FreeboardPostRepository freeboardPostRepository; // 게시글 존재 여부 확인 등에 필요할 수 있음
    private final FreeboardPostRepository freeboardPostRepository; // 게시글 조회를 위해 추가
    private final UserRepository userRepository; // 사용자 조회를 위해 추가

    @Override
    public PageResponseDTO<FreeboardCommentResponseDTO> getCommentsByPostId(Integer postId, Pageable pageable) {
        // 게시글 존재 여부를 먼저 확인하는 로직을 추가할 수 있습니다.
        // 예: if (!freeboardPostRepository.existsById(postId)) { throw new EntityNotFoundException("게시글을 찾을 수 없습니다: " + postId); }

        Page<FreeboardComment> commentPage = freeboardCommentRepository.findByFreeboardPost_PostId(postId, pageable);

        List<FreeboardCommentResponseDTO> dtoList = commentPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return PageResponseDTO.<FreeboardCommentResponseDTO>builder()
                .content(dtoList)
                .currentPage(commentPage.getNumber() + 1) // Page는 0부터 시작
                .totalPages(commentPage.getTotalPages())
                .totalElements(commentPage.getTotalElements())
                .size(commentPage.getSize())
                .first(commentPage.isFirst())
                .last(commentPage.isLast())
                .empty(commentPage.isEmpty())
                .build();
    }

    @Override
    @Transactional // 데이터 생성(쓰기) 작업
    public FreeboardCommentResponseDTO createComment(Integer postId, FreeboardCommentRequestDTO requestDto) {
        // 1. 댓글을 달 게시글(FreeboardPost) 조회
        FreeboardPost targetPost = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 작성할 게시글을 찾을 수 없습니다: " + postId));

        // 2. 댓글 작성자(User) 정보 조회
        // DTO에 userId가 포함되어 있다고 가정 (인증 연동 전 임시 처리)
        User author = userRepository.findUserByUserId(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("댓글 작성자 정보를 찾을 수 없습니다: " + requestDto.getUserId()));

        // 3. DTO를 Entity로 변환하여 새 댓글 생성
        FreeboardComment newComment = FreeboardComment.builder()
                .commentContent(requestDto.getCommentContent())
                .user(author)           // 작성자 엔티티 설정
                .freeboardPost(targetPost) // 대상 게시글 엔티티 설정
                // likeCount, isHidden 등은 기본값으로 설정됨 (엔티티 정의에 따라)
                .build();

        // 4. 댓글 저장
        FreeboardComment savedComment = freeboardCommentRepository.save(newComment);

        // 5. 저장된 Entity를 Response DTO로 변환하여 반환
        return convertToDto(savedComment);
    }

    /**
     * FreeboardComment 엔티티를 FreeboardCommentResponseDTO로 변환합니다.
     *
     * @param comment FreeboardComment 엔티티
     * @return FreeboardCommentResponseDTO
     */
    private FreeboardCommentResponseDTO convertToDto(FreeboardComment comment) {
        User user = comment.getUser(); // @EntityGraph로 인해 추가 쿼리 발생 안 함 (또는 LAZY 로딩 시점)
        Integer postId = (comment.getFreeboardPost() != null) ? comment.getFreeboardPost().getPostId() : null;

        return FreeboardCommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .commentContent(comment.getCommentContent())
                .commentLikeCount(comment.getCommentLikeCount())
                .commentIsHidden(comment.isCommentIsHidden()) // 엔티티 필드명 확인
                .commentCreatedAt(comment.getCommentCreatedAt())
                .commentUpdatedAt(comment.getCommentUpdatedAt())
                .userId(user != null ? user.getUserId() : null)
                .userNickName(user != null ? user.getUserNickName() : "알 수 없는 사용자")
                .postId(postId)
                .build();
    }
}