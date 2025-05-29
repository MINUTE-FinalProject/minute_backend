package com.minute.board.free.service;

// 기존 import 문들 ...
import com.minute.auth.service.DetailUser; // DetailUser import 추가 (SecurityContextHolder 사용 시)
import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.common.dto.response.ReportSuccessResponseDTO;
import com.minute.board.free.dto.request.*;
import com.minute.board.free.dto.response.AdminReportedCommentEntryDTO;
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
import com.minute.board.free.repository.specification.FreeboardCommentSpecification;
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication; // Authentication import 추가
import org.springframework.security.core.context.SecurityContextHolder; // SecurityContextHolder import 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
// import java.time.LocalTime; // getReportedComments 에서만 사용, 여기서는 불필요시 제거 가능
import java.util.Collections; // Collections.emptySet() 사용
import java.util.List;
import java.util.Optional;
import java.util.Set; // Set import 추가
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreeboardCommentServiceImpl implements FreeboardCommentService {

    private final FreeboardCommentRepository freeboardCommentRepository;
    private final FreeboardPostRepository freeboardPostRepository;
    private final UserRepository userRepository;
    private final FreeboardCommentLikeRepository freeboardCommentLikeRepository;
    private final FreeboardCommentReportRepository freeboardCommentReportRepository;

    // 현재 로그인한 사용자 ID를 가져오는 헬퍼 메서드
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String &&
                        authentication.getPrincipal().equals("anonymousUser"))) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof DetailUser) {
                DetailUser detailUser = (DetailUser) principal;
                if (detailUser.getUser() != null) {
                    return detailUser.getUser().getUserId();
                }
            } else if (principal instanceof String) { // 만약 UserDetails가 아닌 String ID를 직접 저장하는 경우 (일반적이지 않음)
                return (String) principal;
            }
        }
        return null;
    }

    @Override
    public PageResponseDTO<FreeboardCommentResponseDTO> getCommentsByPostId(Integer postId, Pageable pageable) {
        Page<FreeboardComment> commentPage = freeboardCommentRepository.findByFreeboardPost_PostId(postId, pageable);
        List<FreeboardComment> comments = commentPage.getContent();

        Set<Integer> likedCommentIds = Collections.emptySet();
        String currentUserId = getCurrentUserId();

        if (currentUserId != null && !comments.isEmpty()) {
            List<Integer> commentIds = comments.stream().map(FreeboardComment::getCommentId).collect(Collectors.toList());
            likedCommentIds = freeboardCommentLikeRepository.findLikedCommentIdsByUserIdAndCommentIdsIn(currentUserId, commentIds);
        }

        final Set<Integer> finalLikedCommentIds = likedCommentIds; // 람다에서 사용하기 위함
        List<FreeboardCommentResponseDTO> dtoList = comments.stream()
                .map(comment -> convertToDto(comment, finalLikedCommentIds)) // 수정된 convertToDto 호출
                .collect(Collectors.toList());

        return PageResponseDTO.<FreeboardCommentResponseDTO>builder()
                .content(dtoList)
                .currentPage(commentPage.getNumber() + 1) // 프론트엔드는 1-based
                .totalPages(commentPage.getTotalPages())
                .totalElements(commentPage.getTotalElements())
                .size(commentPage.getSize())
                .first(commentPage.isFirst())
                .last(commentPage.isLast())
                .empty(commentPage.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public FreeboardCommentResponseDTO createComment(Integer postId, FreeboardCommentRequestDTO requestDto, String currentUserIdFromController) {
        FreeboardPost targetPost = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 작성할 게시글을 찾을 수 없습니다: " + postId));

        User author = userRepository.findUserByUserId(currentUserIdFromController)
                .orElseThrow(() -> new EntityNotFoundException("댓글 작성자 정보를 찾을 수 없습니다: " + currentUserIdFromController));

        FreeboardComment newComment = FreeboardComment.builder()
                .commentContent(requestDto.getCommentContent())
                .user(author)
                .freeboardPost(targetPost)
                .build();

        FreeboardComment savedComment = freeboardCommentRepository.save(newComment);
        // 새로 생성된 댓글은 현재 사용자가 좋아요를 누르지 않았다고 가정 (또는 필요시 여기서 확인)
        return convertToDto(savedComment, Collections.emptySet());
    }

    @Override
    @Transactional
    public FreeboardCommentResponseDTO updateComment(Integer commentId, FreeboardCommentRequestDTO requestDto, String currentUserIdFromController) {
        FreeboardComment commentToUpdate = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 댓글을 찾을 수 없습니다: " + commentId));

        if (!commentToUpdate.getUser().getUserId().equals(currentUserIdFromController)) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        commentToUpdate.setCommentContent(requestDto.getCommentContent());
        // 수정된 댓글의 좋아요 상태는 기존 상태를 유지하거나, 필요시 여기서 확인
        // 여기서는 간단히 현재 사용자의 좋아요 정보를 다시 조회하지 않고, 기존 convertToDto의 기본값을 따르도록 함.
        // 만약 수정 후에도 정확한 isLikedByCurrentUser가 필요하면, currentUserIdFromController와 commentId로 좋아요 상태 조회 로직 추가 필요.
        // 지금은 목록에서 가져온 isLikedByCurrentUser가 없으므로, 그냥 emptySet 전달.
        return convertToDto(commentToUpdate, Collections.emptySet()); // 또는 해당 댓글의 좋아요 상태를 조회하여 전달
    }

    @Override
    @Transactional
    public void deleteComment(Integer commentId, String currentUserIdFromController) {
        FreeboardComment commentToDelete = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 댓글을 찾을 수 없습니다: " + commentId));

        if (!commentToDelete.getUser().getUserId().equals(currentUserIdFromController) /* && !isAdmin */) { // 관리자 삭제 권한은 주석처리된 대로 추가 가능
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        freeboardCommentRepository.delete(commentToDelete);
    }

    @Override
    @Transactional
    public CommentLikeResponseDTO toggleCommentLike(Integer commentId, String currentUserIdFromController) {
        FreeboardComment comment = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("좋아요를 누를 댓글을 찾을 수 없습니다: " + commentId));

        User user = userRepository.findUserByUserId(currentUserIdFromController)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다: " + currentUserIdFromController));

        Optional<FreeboardCommentLike> existingLike = freeboardCommentLikeRepository.findByUserAndFreeboardComment(user, comment);
        boolean likedByCurrentUser;

        if (existingLike.isPresent()) {
            freeboardCommentLikeRepository.delete(existingLike.get());
            comment.setCommentLikeCount(Math.max(0, comment.getCommentLikeCount() - 1));
            likedByCurrentUser = false;
        } else {
            FreeboardCommentLike newLike = FreeboardCommentLike.builder()
                    .user(user)
                    .freeboardComment(comment)
                    .build();
            freeboardCommentLikeRepository.save(newLike);
            comment.setCommentLikeCount(comment.getCommentLikeCount() + 1);
            likedByCurrentUser = true;
        }
        // comment 엔티티의 변경사항은 @Transactional 에 의해 커밋 시점에 DB에 반영됨

        return CommentLikeResponseDTO.builder()
                .commentId(comment.getCommentId())
                .currentLikeCount(comment.getCommentLikeCount())
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }

    @Override
    @Transactional
    public ReportSuccessResponseDTO reportComment(Integer commentId, String currentUserIdFromController) {
        FreeboardComment commentToReport = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("신고할 댓글을 찾을 수 없습니다: " + commentId));

        User reporter = userRepository.findUserByUserId(currentUserIdFromController)
                .orElseThrow(() -> new EntityNotFoundException("신고자 정보를 찾을 수 없습니다: " + currentUserIdFromController));

        if (commentToReport.getUser().getUserId().equals(reporter.getUserId())) {
            throw new IllegalStateException("자신의 댓글은 신고할 수 없습니다.");
        }
        boolean alreadyReported = freeboardCommentReportRepository.existsByUserAndFreeboardComment(reporter, commentToReport);
        if (alreadyReported) {
            throw new IllegalStateException("이미 신고한 댓글입니다.");
        }

        FreeboardCommentReport newReport = FreeboardCommentReport.builder()
                .user(reporter)
                .freeboardComment(commentToReport)
                .build();
        freeboardCommentReportRepository.save(newReport);
        return new ReportSuccessResponseDTO("댓글이 성공적으로 신고되었습니다.", commentId);
    }

    @Override
    public PageResponseDTO<AdminReportedCommentEntryDTO> getReportedComments(AdminReportedCommentFilterDTO filter, Pageable pageable) {
        AdminReportedCommentFilterDTO queryFilter = new AdminReportedCommentFilterDTO();
        queryFilter.setSearchKeyword(filter.getSearchKeyword());
        queryFilter.setOriginalPostId(filter.getOriginalPostId());
        queryFilter.setAuthorUserId(filter.getAuthorUserId());
        queryFilter.setAuthorNickname(filter.getAuthorNickname());
        queryFilter.setIsHidden(filter.getIsHidden());

        if (filter.getReportStartDate() != null) {
            queryFilter.setQueryReportStartDate(filter.getReportStartDate().atStartOfDay());
        }
        if (filter.getReportEndDate() != null) {
            // 종료일의 마지막 시간까지 포함하도록 하거나, 다음날 0시 미만으로 설정
            queryFilter.setQueryReportEndDate(filter.getReportEndDate().plusDays(1).atStartOfDay());
        }

        Page<AdminReportedCommentEntryDTO> reportedCommentPage = freeboardCommentReportRepository.findReportedCommentSummariesWithFilters(queryFilter, pageable);

        return PageResponseDTO.<AdminReportedCommentEntryDTO>builder()
                .content(reportedCommentPage.getContent())
                .currentPage(reportedCommentPage.getNumber() + 1)
                .totalPages(reportedCommentPage.getTotalPages())
                .totalElements(reportedCommentPage.getTotalElements())
                .size(reportedCommentPage.getSize())
                .first(reportedCommentPage.isFirst())
                .last(reportedCommentPage.isLast())
                .empty(reportedCommentPage.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public FreeboardCommentResponseDTO updateCommentVisibility(Integer commentId, CommentVisibilityRequestDTO requestDto) {
        FreeboardComment comment = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("상태를 변경할 댓글을 찾을 수 없습니다: " + commentId));
        comment.setCommentIsHidden(requestDto.getIsHidden());
        // isLikedByCurrentUser는 이 맥락에서는 알 수 없으므로 기본값 false 처리
        return convertToDto(comment, Collections.emptySet());
    }

    @Override
    public PageResponseDTO<FreeboardCommentResponseDTO> getCommentsByAuthor(String currentUserIdFromController, @Nullable AdminMyCommentFilterDTO filter, Pageable pageable) {
        User author = userRepository.findUserByUserId(currentUserIdFromController)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다: " + currentUserIdFromController));

        Specification<FreeboardComment> spec = Specification.where(FreeboardCommentSpecification.hasAuthor(author));

        if (filter != null) {
            if (StringUtils.hasText(filter.getSearchKeyword())) {
                spec = spec.and(FreeboardCommentSpecification.contentContains(filter.getSearchKeyword()));
            }
            if (filter.getStartDate() != null) {
                spec = spec.and(FreeboardCommentSpecification.createdAtAfter(filter.getStartDate()));
            }
            if (filter.getEndDate() != null) {
                spec = spec.and(FreeboardCommentSpecification.createdAtBefore(filter.getEndDate()));
            }
        }

        Page<FreeboardComment> commentPage = freeboardCommentRepository.findAll(spec, pageable);
        List<FreeboardComment> comments = commentPage.getContent();

        Set<Integer> likedCommentIds = Collections.emptySet();
        // getCommentsByAuthor는 currentUserIdFromController를 이미 알고 있으므로 이를 사용
        if (!comments.isEmpty()) {
            List<Integer> commentIds = comments.stream().map(FreeboardComment::getCommentId).collect(Collectors.toList());
            likedCommentIds = freeboardCommentLikeRepository.findLikedCommentIdsByUserIdAndCommentIdsIn(currentUserIdFromController, commentIds);
        }

        final Set<Integer> finalLikedCommentIds = likedCommentIds;
        List<FreeboardCommentResponseDTO> dtoList = comments.stream()
                .map(comment -> convertToDto(comment, finalLikedCommentIds))
                .collect(Collectors.toList());

        return PageResponseDTO.<FreeboardCommentResponseDTO>builder()
                .content(dtoList)
                .currentPage(commentPage.getNumber() + 1)
                .totalPages(commentPage.getTotalPages())
                .totalElements(commentPage.getTotalElements())
                .size(commentPage.getSize())
                .first(commentPage.isFirst())
                .last(commentPage.isLast())
                .empty(commentPage.isEmpty())
                .build();
    }

    // convertToDto 메서드 시그니처 변경 및 로직 수정
    private FreeboardCommentResponseDTO convertToDto(FreeboardComment comment, Set<Integer> likedCommentIdsForCurrentUser) {
        User user = comment.getUser(); // 댓글 작성자
        Integer postId = (comment.getFreeboardPost() != null) ? comment.getFreeboardPost().getPostId() : null;

        boolean isLiked = likedCommentIdsForCurrentUser.contains(comment.getCommentId());

        return FreeboardCommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .commentContent(comment.getCommentContent())
                .commentLikeCount(comment.getCommentLikeCount())
                .commentIsHidden(comment.isCommentIsHidden())
                .commentCreatedAt(comment.getCommentCreatedAt())
                .commentUpdatedAt(comment.getCommentUpdatedAt())
                .userId(user != null ? user.getUserId() : null)
                .userNickName(user != null ? user.getUserNickName() : "알 수 없는 사용자")
                .postId(postId)
                .isLikedByCurrentUser(isLiked) // 필드 설정
                .build();
    }
}