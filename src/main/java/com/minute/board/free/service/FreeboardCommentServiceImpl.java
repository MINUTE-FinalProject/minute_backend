package com.minute.board.free.service;

import com.minute.auth.service.DetailUser;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
// import java.time.LocalTime; // 필요시 주석 해제
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
            }
        }
        return null;
    }

    private User getCurrentUserEntity() {
        String currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            return userRepository.findUserByUserId(currentUserId).orElse(null);
        }
        return null;
    }

    @Override
    public PageResponseDTO<FreeboardCommentResponseDTO> getCommentsByPostId(Integer postId, Pageable pageable) {
        Page<FreeboardComment> commentPage = freeboardCommentRepository.findByFreeboardPost_PostId(postId, pageable);
        List<FreeboardComment> comments = commentPage.getContent();

        Set<Integer> likedCommentIds = Collections.emptySet();
        Set<Integer> reportedCommentIds = Collections.emptySet();
        String currentUserId = getCurrentUserId();

        if (currentUserId != null && !comments.isEmpty()) {
            List<Integer> commentIds = comments.stream().map(FreeboardComment::getCommentId).collect(Collectors.toList());
            likedCommentIds = freeboardCommentLikeRepository.findLikedCommentIdsByUserIdAndCommentIdsIn(currentUserId, commentIds);
            reportedCommentIds = freeboardCommentReportRepository.findReportedCommentIdsByUserIdAndCommentIdsIn(currentUserId, commentIds);
        }

        final Set<Integer> finalLikedCommentIds = likedCommentIds;
        final Set<Integer> finalReportedCommentIds = reportedCommentIds;

        List<FreeboardCommentResponseDTO> dtoList = comments.stream()
                .map(comment -> convertToDto(comment, finalLikedCommentIds, finalReportedCommentIds))
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
        return convertToDto(savedComment, Collections.emptySet(), Collections.emptySet());
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

        boolean isLiked = false;
        boolean isReported = false;
        User currentUser = getCurrentUserEntity();
        if (currentUser != null && currentUser.getUserId().equals(currentUserIdFromController)) { // 현재 사용자가 수정 요청한 사용자인지 한번 더 확인
            isLiked = freeboardCommentLikeRepository.findByUserAndFreeboardComment(currentUser, commentToUpdate).isPresent();
            isReported = freeboardCommentReportRepository.existsByUserAndFreeboardComment(currentUser, commentToUpdate);
        }
        Set<Integer> likedIds = isLiked ? Set.of(commentId) : Collections.emptySet();
        Set<Integer> reportedIds = isReported ? Set.of(commentId) : Collections.emptySet();

        return convertToDto(commentToUpdate, likedIds, reportedIds);
    }

    @Override
    @Transactional
    public void deleteComment(Integer commentId, String currentUserIdFromController) {
        FreeboardComment commentToDelete = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 댓글을 찾을 수 없습니다: " + commentId));

        if (!commentToDelete.getUser().getUserId().equals(currentUserIdFromController) /* && !isAdmin(currentUserIdFromController) */ ) { // 관리자 권한 확인 로직 추가 필요시
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
        freeboardCommentRepository.save(comment); // 좋아요 수 변경사항 저장

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

        // ✅ 신고 당한 사용자의 신고 횟수 증가
        User reportedUser = commentToReport.getUser();
        reportedUser.setUserReport(reportedUser.getUserReport() + 1);
        userRepository.save(reportedUser);

        return new ReportSuccessResponseDTO("댓글이 성공적으로 신고되었습니다.", commentId);
    }

    @Override
    public PageResponseDTO<AdminReportedCommentEntryDTO> getReportedComments(AdminReportedCommentFilterDTO filter, Pageable pageable) {
        System.out.println("Service getReportedComments - Pageable Sort: " + pageable.getSort());
        AdminReportedCommentFilterDTO queryFilter = new AdminReportedCommentFilterDTO();
        queryFilter.setSearchKeyword(filter.getSearchKeyword());
        queryFilter.setOriginalPostId(filter.getOriginalPostId());
        queryFilter.setAuthorUserId(filter.getAuthorUserId());
        queryFilter.setAuthorNickname(filter.getAuthorNickname());
        queryFilter.setIsHidden(filter.getIsHidden());

        // --- 날짜 필터링 로직 수정 ---
        // DTO에서 변경된 필드명(commentCreatedAtStartDate, commentCreatedAtEndDate)을 사용합니다.
        if (filter.getCommentCreatedAtStartDate() != null) {
            queryFilter.setQueryCommentCreatedAtStartDate(filter.getCommentCreatedAtStartDate().atStartOfDay());
        }
        if (filter.getCommentCreatedAtEndDate() != null) {
            // 끝 날짜를 포함하여 검색하기 위해 +1일 후 시작 시간 미만으로 설정 (JPQL에서 '<' 사용 시)
            queryFilter.setQueryCommentCreatedAtEndDate(filter.getCommentCreatedAtEndDate().plusDays(1).atStartOfDay());
        }
        // --- 날짜 필터링 로직 수정 끝 ---

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
        freeboardCommentRepository.save(comment); // 변경사항 저장

        boolean isLiked = false;
        boolean isReported = false;
        User currentUser = getCurrentUserEntity();
        if (currentUser != null) {
            isLiked = freeboardCommentLikeRepository.findByUserAndFreeboardComment(currentUser, comment).isPresent();
            isReported = freeboardCommentReportRepository.existsByUserAndFreeboardComment(currentUser, comment);
        }
        Set<Integer> likedIds = isLiked ? Set.of(commentId) : Collections.emptySet();
        Set<Integer> reportedIds = isReported ? Set.of(commentId) : Collections.emptySet();

        return convertToDto(comment, likedIds, reportedIds);
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
            // AdminMyCommentFilterDTO의 startDate와 endDate는 댓글 작성일 기준입니다.
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
        Set<Integer> reportedCommentIds = Collections.emptySet();

        if (!comments.isEmpty()) {
            List<Integer> commentIds = comments.stream().map(FreeboardComment::getCommentId).collect(Collectors.toList());
            likedCommentIds = freeboardCommentLikeRepository.findLikedCommentIdsByUserIdAndCommentIdsIn(currentUserIdFromController, commentIds);
            reportedCommentIds = freeboardCommentReportRepository.findReportedCommentIdsByUserIdAndCommentIdsIn(currentUserIdFromController, commentIds);
        }

        final Set<Integer> finalLikedCommentIds = likedCommentIds;
        final Set<Integer> finalReportedCommentIds = reportedCommentIds;

        List<FreeboardCommentResponseDTO> dtoList = comments.stream()
                .map(comment -> convertToDto(comment, finalLikedCommentIds, finalReportedCommentIds))
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

    private FreeboardCommentResponseDTO convertToDto(FreeboardComment comment,
                                                     Set<Integer> likedCommentIdsForCurrentUser,
                                                     Set<Integer> reportedCommentIdsForCurrentUser) {
        User author = comment.getUser();
        Integer postId = (comment.getFreeboardPost() != null) ? comment.getFreeboardPost().getPostId() : null;

        boolean isLiked = likedCommentIdsForCurrentUser.contains(comment.getCommentId());
        boolean isReported = reportedCommentIdsForCurrentUser.contains(comment.getCommentId());
        // User 엔티티의 Role enum이 null일 경우를 대비하여 null 체크 추가
        String authorRole = (author != null && author.getRole() != null) ? author.getRole().name() : "USER"; // 기본값 또는 예외 처리

        return FreeboardCommentResponseDTO.builder()
                .commentId(comment.getCommentId())
                .commentContent(comment.getCommentContent())
                .commentLikeCount(comment.getCommentLikeCount())
                .commentIsHidden(comment.isCommentIsHidden())
                .commentCreatedAt(comment.getCommentCreatedAt())
                .commentUpdatedAt(comment.getCommentUpdatedAt())
                .userId(author != null ? author.getUserId() : null)
                .userNickName(author != null ? author.getUserNickName() : "알 수 없는 사용자")
                .postId(postId)
                .isLikedByCurrentUser(isLiked)
                .isReportedByCurrentUser(isReported)
                .authorRole(authorRole)
                .build();
    }
}