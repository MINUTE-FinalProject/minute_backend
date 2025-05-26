package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.common.dto.response.ReportSuccessResponseDTO;
import com.minute.board.free.dto.request.CommentLikeRequestDTO;
import com.minute.board.free.dto.request.CommentReportRequestDTO;
import com.minute.board.free.dto.request.FreeboardCommentRequestDTO;
import com.minute.board.free.dto.response.CommentLikeResponseDTO;
import com.minute.board.free.dto.response.FreeboardCommentResponseDTO;
import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardCommentLike;
import com.minute.board.free.entity.FreeboardCommentReport;
import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.repository.FreeboardCommentLikeRepository;
import com.minute.board.free.repository.FreeboardCommentReportRepository;
import com.minute.board.free.repository.FreeboardCommentRepository;
import com.minute.board.free.repository.FreeboardPostRepository;
import com.minute.user.entity.User; // User 엔티티 import
import com.minute.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreeboardCommentServiceImpl implements FreeboardCommentService {

    private final FreeboardCommentRepository freeboardCommentRepository;
    // private final FreeboardPostRepository freeboardPostRepository; // 게시글 존재 여부 확인 등에 필요할 수 있음
    private final FreeboardPostRepository freeboardPostRepository; // 게시글 조회를 위해 추가
    private final UserRepository userRepository; // 사용자 조회를 위해 추가
    private final FreeboardCommentLikeRepository freeboardCommentLikeRepository; // 주입 추가
    private final FreeboardCommentReportRepository freeboardCommentReportRepository; // 주입 추가

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

    @Override
    @Transactional // 데이터 변경(수정) 작업
    public FreeboardCommentResponseDTO updateComment(Integer commentId, FreeboardCommentRequestDTO requestDto) {
        // 1. 수정할 댓글 조회
        FreeboardComment commentToUpdate = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 댓글을 찾을 수 없습니다: " + commentId));

        // 2. (임시) 수정 권한 확인: 요청 DTO의 userId와 실제 댓글 작성자의 userId가 일치하는지 확인
        //    실제 인증 연동 시에는 SecurityContextHolder에서 현재 로그인한 사용자 정보를 가져와 비교해야 합니다.
        String requestUserId = requestDto.getUserId(); // 수정을 시도하는 사용자의 ID (DTO에서 임시로 받음)
        if (requestUserId == null || !commentToUpdate.getUser().getUserId().equals(requestUserId)) {
            // 실제로는 관리자(Admin)도 수정 가능하도록 로직 추가 필요
            throw new AccessDeniedException("댓글 수정 권한이 없습니다. (작성자 불일치)");
        }
        // User updater = userRepository.findUserByUserId(requestUserId)
        //        .orElseThrow(() -> new EntityNotFoundException("수정자 정보를 찾을 수 없습니다: " + requestUserId));
        // 위 라인은 requestUserId가 유효한 사용자인지 한번 더 체크하는 용도.

        // 3. 댓글 내용 업데이트 (JPA의 dirty checking 활용)
        commentToUpdate.setCommentContent(requestDto.getCommentContent());
        // commentUpdatedAt 필드는 @UpdateTimestamp 어노테이션에 의해 자동 업데이트됩니다.

        // freeboardCommentRepository.save(commentToUpdate); // @Transactional에 의해 자동 업데이트, 명시적 save 불필요

        // 4. 수정된 Entity를 Response DTO로 변환하여 반환
        return convertToDto(commentToUpdate);
    }

    @Override
    @Transactional // 데이터 삭제 작업
    public void deleteComment(Integer commentId, String requestUserId) {
        // 1. 삭제할 댓글 조회
        FreeboardComment commentToDelete = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 댓글을 찾을 수 없습니다: " + commentId));

        // 2. (임시) 삭제 권한 확인: 요청된 userId와 실제 댓글 작성자의 userId가 일치하는지 확인
        //    실제 인증 연동 시에는 SecurityContextHolder에서 현재 로그인한 사용자 정보를 가져와 비교해야 합니다.
        if (requestUserId == null || !commentToDelete.getUser().getUserId().equals(requestUserId)) {
            // 실제로는 관리자(Admin)도 삭제 가능하도록 로직 추가 필요
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다. (작성자 불일치)");
        }

        // 3. 댓글 삭제
        // FreeboardCommentLike, FreeboardCommentReport 등 연관 엔티티는
        // DB 스키마에서 ON DELETE CASCADE로 설정되어 있다면 댓글 삭제 시 자동으로 함께 삭제됩니다.
        freeboardCommentRepository.delete(commentToDelete);
    }

    @Override
    @Transactional // 데이터 변경(좋아요 추가/삭제 및 댓글 좋아요 수 업데이트)
    public CommentLikeResponseDTO toggleCommentLike(Integer commentId, CommentLikeRequestDTO requestDto) {
        // 1. 댓글 조회
        FreeboardComment comment = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("좋아요를 누를 댓글을 찾을 수 없습니다: " + commentId));

        // 2. 사용자 조회
        User user = userRepository.findUserByUserId(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다: " + requestDto.getUserId()));

        // 3. 이미 좋아요를 눌렀는지 확인
        Optional<FreeboardCommentLike> existingLike = freeboardCommentLikeRepository.findByUserAndFreeboardComment(user, comment);

        boolean likedByCurrentUser;

        if (existingLike.isPresent()) {
            // 이미 좋아요를 눌렀다면 -> 좋아요 취소
            freeboardCommentLikeRepository.delete(existingLike.get());
            comment.setCommentLikeCount(Math.max(0, comment.getCommentLikeCount() - 1)); // 좋아요 수 감소
            likedByCurrentUser = false;
        } else {
            // 좋아요를 누르지 않았다면 -> 좋아요 추가
            FreeboardCommentLike newLike = FreeboardCommentLike.builder()
                    .user(user)
                    .freeboardComment(comment)
                    .build();
            freeboardCommentLikeRepository.save(newLike);
            comment.setCommentLikeCount(comment.getCommentLikeCount() + 1); // 좋아요 수 증가
            likedByCurrentUser = true;
        }
        // FreeboardComment의 변경된 commentLikeCount는 @Transactional에 의해 자동 저장됨

        return CommentLikeResponseDTO.builder()
                .commentId(comment.getCommentId())
                .currentLikeCount(comment.getCommentLikeCount())
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }

    @Override
    @Transactional // 데이터 생성(신고 기록)
    public ReportSuccessResponseDTO reportComment(Integer commentId, CommentReportRequestDTO requestDto) {
        // 1. 댓글 조회
        FreeboardComment commentToReport = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("신고할 댓글을 찾을 수 없습니다: " + commentId));

        // 2. 신고자 조회
        User reporter = userRepository.findUserByUserId(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("신고자 정보를 찾을 수 없습니다: " + requestDto.getUserId()));

        // 3. 자신의 댓글인지 확인
        if (commentToReport.getUser().getUserId().equals(reporter.getUserId())) {
            throw new IllegalStateException("자신의 댓글은 신고할 수 없습니다.");
        }

        // 4. 이미 신고했는지 확인 (DB의 UNIQUE 제약 조건(uk_fcr_user_comment)으로도 방지되지만, 미리 확인)
        // FreeboardCommentReportRepository에 existsByUserAndFreeboardComment 메서드 필요
        boolean alreadyReported = freeboardCommentReportRepository.existsByUserAndFreeboardComment(reporter, commentToReport);
        if (alreadyReported) {
            throw new IllegalStateException("이미 신고한 댓글입니다.");
        }

        // 5. 신고 기록 생성 및 저장
        FreeboardCommentReport newReport = FreeboardCommentReport.builder()
                .user(reporter)
                .freeboardComment(commentToReport)
                // comment_report_date는 @CreationTimestamp로 자동 생성
                .build();
        freeboardCommentReportRepository.save(newReport);

        return new ReportSuccessResponseDTO("댓글이 성공적으로 신고되었습니다.", commentId);
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