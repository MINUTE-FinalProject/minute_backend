package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.common.dto.response.ReportSuccessResponseDTO;
import com.minute.board.free.dto.request.*;
import com.minute.board.free.dto.response.AdminReportedCommentEntryDTO;
import com.minute.board.free.dto.response.CommentLikeResponseDTO;
import com.minute.board.free.dto.response.FreeboardCommentResponseDTO;
import com.minute.board.free.dto.response.ReportedCommentEntryDTO; // 이 DTO는 현재 이 클래스에서 직접 사용되지 않음
import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardCommentLike;
import com.minute.board.free.entity.FreeboardCommentReport;
import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.repository.FreeboardCommentLikeRepository;
import com.minute.board.free.repository.FreeboardCommentReportRepository;
import com.minute.board.free.repository.FreeboardCommentRepository;
import com.minute.board.free.repository.FreeboardPostRepository;
import com.minute.board.free.repository.specification.FreeboardCommentSpecification;
import com.minute.user.entity.User; // User 엔티티 import
import com.minute.user.enumpackage.Role; // <<< Role enum 임포트 (관리자 역할 확인 등에 필요시)
import com.minute.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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

    @Override
    public PageResponseDTO<FreeboardCommentResponseDTO> getCommentsByPostId(Integer postId, Pageable pageable) {
        // 이 메소드는 공개 API용이므로 currentUserId를 받지 않습니다.
        Page<FreeboardComment> commentPage = freeboardCommentRepository.findByFreeboardPost_PostId(postId, pageable);
        List<FreeboardCommentResponseDTO> dtoList = commentPage.getContent().stream()
                .map(this::convertToDto)
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
    public FreeboardCommentResponseDTO createComment(Integer postId, FreeboardCommentRequestDTO requestDto, String currentUserId) { // <<< currentUserId 파라미터 추가
        FreeboardPost targetPost = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 작성할 게시글을 찾을 수 없습니다: " + postId));

        // 댓글 작성자(User) 정보 조회 (이제 DTO가 아닌 currentUserId 사용)
        User author = userRepository.findUserByUserId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("댓글 작성자 정보를 찾을 수 없습니다: " + currentUserId));

        FreeboardComment newComment = FreeboardComment.builder()
                .commentContent(requestDto.getCommentContent())
                .user(author)           // 인증된 사용자로 작성자 설정
                .freeboardPost(targetPost)
                .build();

        FreeboardComment savedComment = freeboardCommentRepository.save(newComment);
        return convertToDto(savedComment);
    }

    @Override
    @Transactional
    public FreeboardCommentResponseDTO updateComment(Integer commentId, FreeboardCommentRequestDTO requestDto, String currentUserId /*, DetailUser principal - 역할 확인 필요시 */) { // <<< currentUserId 파라미터 추가
        FreeboardComment commentToUpdate = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 댓글을 찾을 수 없습니다: " + commentId));

        // User currentUser = userRepository.findUserByUserId(currentUserId).orElseThrow(...); // 필요시 현재 사용자 엔티티 조회
        // boolean isAdmin = principal != null && principal.getUser().getRole() == Role.ADMIN; // 예시: 관리자 여부 확인

        // 수정 권한 확인: 요청한 사용자(currentUserId)와 실제 댓글 작성자의 userId가 일치하는지 확인
        // 또는 관리자(Admin)도 수정 가능하도록 로직 추가 가능
        if (!commentToUpdate.getUser().getUserId().equals(currentUserId) /* && !isAdmin */) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        commentToUpdate.setCommentContent(requestDto.getCommentContent());
        return convertToDto(commentToUpdate);
    }

    @Override
    @Transactional
    public void deleteComment(Integer commentId, String currentUserId /*, DetailUser principal - 역할 확인 필요시 */) { // <<< requestUserId를 currentUserId로 변경
        FreeboardComment commentToDelete = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 댓글을 찾을 수 없습니다: " + commentId));

        // User currentUser = userRepository.findUserByUserId(currentUserId).orElseThrow(...);
        // boolean isAdmin = principal != null && principal.getUser().getRole() == Role.ADMIN;

        // 삭제 권한 확인
        if (!commentToDelete.getUser().getUserId().equals(currentUserId) /* && !isAdmin */) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        freeboardCommentRepository.delete(commentToDelete);
    }

    @Override
    @Transactional
    public CommentLikeResponseDTO toggleCommentLike(Integer commentId, String currentUserId) { // <<< CommentLikeRequestDTO requestDto 제거, currentUserId 파라미터 추가
        FreeboardComment comment = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("좋아요를 누를 댓글을 찾을 수 없습니다: " + commentId));

        // 사용자 조회 (이제 DTO가 아닌 currentUserId 사용)
        User user = userRepository.findUserByUserId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다: " + currentUserId));

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

        return CommentLikeResponseDTO.builder()
                .commentId(comment.getCommentId())
                .currentLikeCount(comment.getCommentLikeCount())
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }

    @Override
    @Transactional
    public ReportSuccessResponseDTO reportComment(Integer commentId, String currentUserId) { // <<< CommentReportRequestDTO requestDto 제거, currentUserId 파라미터 추가
        FreeboardComment commentToReport = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("신고할 댓글을 찾을 수 없습니다: " + commentId));

        // 신고자 조회 (이제 DTO가 아닌 currentUserId 사용)
        User reporter = userRepository.findUserByUserId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("신고자 정보를 찾을 수 없습니다: " + currentUserId));

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
        // 이 메소드는 관리자 기능이므로 currentUserId를 직접 받지 않습니다.
        // 접근 제어는 WebSecurityConfig 또는 컨트롤러의 @PreAuthorize 로 이루어집니다.
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
        // 이 메소드는 관리자 기능이므로 currentUserId를 직접 받지 않습니다.
        FreeboardComment comment = freeboardCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("상태를 변경할 댓글을 찾을 수 없습니다: " + commentId));
        comment.setCommentIsHidden(requestDto.getIsHidden());
        return convertToDto(comment);
    }

    @Override
    public PageResponseDTO<FreeboardCommentResponseDTO> getCommentsByAuthor(String currentUserId, @Nullable AdminMyCommentFilterDTO filter, Pageable pageable) { // <<< userId를 currentUserId로 변경
        User author = userRepository.findUserByUserId(currentUserId) // currentUserId 사용
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다: " + currentUserId));

        Specification<FreeboardComment> spec = Specification.where(FreeboardCommentSpecification.hasAuthor(author));

        if (filter != null) {
            LocalDateTime queryStartDate = (filter.getStartDate() != null) ? filter.getStartDate().atStartOfDay() : null;
            LocalDateTime queryEndDate = (filter.getEndDate() != null) ? filter.getEndDate().plusDays(1).atStartOfDay() : null; // 다음 날 자정 미만

            if (StringUtils.hasText(filter.getSearchKeyword())) {
                spec = spec.and(FreeboardCommentSpecification.contentContains(filter.getSearchKeyword()));
            }
            if (queryStartDate != null) {
                spec = spec.and(FreeboardCommentSpecification.createdAtAfter(queryStartDate.toLocalDate())); // LocalDate로 변경
            }
            if (queryEndDate != null) {
                spec = spec.and(FreeboardCommentSpecification.createdAtBefore(queryEndDate.toLocalDate())); // LocalDate로 변경 (다음 날 자정이므로 해당 일자까지 포함)
            }
        }

        Page<FreeboardComment> commentPage = freeboardCommentRepository.findAll(spec, pageable);
        List<FreeboardCommentResponseDTO> dtoList = commentPage.getContent().stream()
                .map(this::convertToDto)
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

    private FreeboardCommentResponseDTO convertToDto(FreeboardComment comment) {
        User user = comment.getUser();
        Integer postId = (comment.getFreeboardPost() != null) ? comment.getFreeboardPost().getPostId() : null;

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
                .build();
    }

    // convertToReportedCommentEntryDto 메서드는 이 서비스에서는 직접 사용되지 않으므로,
    // AdminReportViewServiceImpl로 이동했거나 해당 서비스에만 필요한 경우 여기서는 제거해도 됩니다.
    // 만약 공통 유틸리티라면 별도의 클래스로 분리하는 것이 좋습니다.
    // 여기서는 일단 주석 처리하거나, 필요 없다면 삭제합니다.
    /*
    private ReportedCommentEntryDTO convertToReportedCommentEntryDto(FreeboardCommentReport report) {
        // ... (내용 생략) ...
    }
    */
}