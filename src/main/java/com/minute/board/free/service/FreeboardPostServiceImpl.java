package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.auth.service.DetailUser; // <<< DetailUser 임포트 (관리자 역할 확인 등에 필요시)
import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.common.dto.response.ReportSuccessResponseDTO;
import com.minute.board.free.dto.request.*;
import com.minute.board.free.dto.response.*;
import com.minute.board.free.entity.FreeboardComment;
import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.entity.FreeboardPostLike;
import com.minute.board.free.entity.FreeboardPostReport;
import com.minute.board.free.repository.FreeboardCommentRepository;
import com.minute.board.free.repository.FreeboardPostLikeRepository;
import com.minute.board.free.repository.FreeboardPostReportRepository;
import com.minute.board.free.repository.FreeboardPostRepository;
import com.minute.user.entity.User; // User 엔티티 import (경로 확인 필요)
import com.minute.user.enumpackage.Role; // <<< Role enum 임포트 (관리자 역할 확인 등에 필요시)
import com.minute.user.repository.UserRepository;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.core.GrantedAuthority; // <<< 관리자 역할 확인 등에 필요시
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
// import java.util.Collection; // <<< 관리자 역할 확인 등에 필요시
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreeboardPostServiceImpl implements FreeboardPostService {

    private final FreeboardPostRepository freeboardPostRepository;
    private final UserRepository userRepository;
    private final FreeboardPostLikeRepository freeboardPostLikeRepository;
    private final FreeboardPostReportRepository freeboardPostReportRepository;
    private final FreeboardCommentRepository freeboardCommentRepository;

    @Override
    public PageResponseDTO<FreeboardPostSimpleResponseDTO> getAllPosts(
            Pageable pageable,
            @Nullable String authorUserId,
            @Nullable String searchKeyword,
            @Nullable LocalDate startDate,
            @Nullable LocalDate endDate) {
        // 이 메소드는 공개 API용이므로 currentUserId를 받지 않습니다.
        Specification<FreeboardPost> spec = Specification.where(com.minute.board.free.repository.specification.FreeboardPostSpecification.isNotHidden());

        if (StringUtils.hasText(authorUserId)) {
            spec = spec.and(com.minute.board.free.repository.specification.FreeboardPostSpecification.hasAuthor(authorUserId));
        }
        if (StringUtils.hasText(searchKeyword)) {
            spec = spec.and(com.minute.board.free.repository.specification.FreeboardPostSpecification.combinedSearch(searchKeyword));
        }
        if (startDate != null) {
            spec = spec.and(com.minute.board.free.repository.specification.FreeboardPostSpecification.createdAtAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(com.minute.board.free.repository.specification.FreeboardPostSpecification.createdAtBefore(endDate));
        }

        Page<FreeboardPost> postPage = freeboardPostRepository.findAll(spec, pageable);
        List<FreeboardPostSimpleResponseDTO> dtoList = postPage.getContent().stream()
                .map(this::convertToSimpleDto)
                .collect(Collectors.toList());

        return PageResponseDTO.<FreeboardPostSimpleResponseDTO>builder()
                .content(dtoList)
                .currentPage(postPage.getNumber() + 1)
                .totalPages(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .size(postPage.getSize())
                .first(postPage.isFirst())
                .last(postPage.isLast())
                .empty(postPage.isEmpty())
                .build();
    }


    @Override
    @Transactional
    public FreeboardPostResponseDTO getPostById(Integer postId) {
        // 이 메소드는 공개 API용이므로 currentUserId를 받지 않습니다.
        FreeboardPost post = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + postId));
        post.setPostViewCount(post.getPostViewCount() + 1);
        return convertToDetailDto(post);
    }

    @Override
    @Transactional
    public FreeboardPostResponseDTO createPost(FreeboardPostRequestDTO requestDto, String currentUserId) { // <<< currentUserId 파라미터 추가
        // 1. 작성자(User) 정보 조회 (이제 DTO가 아닌 currentUserId 사용)
        User author = userRepository.findUserByUserId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("작성자 정보를 찾을 수 없습니다: " + currentUserId));

        // 2. DTO를 Entity로 변환
        FreeboardPost newPost = FreeboardPost.builder()
                .postTitle(requestDto.getPostTitle())
                .postContent(requestDto.getPostContent())
                .user(author) // 인증된 사용자로 작성자 설정
                .build();

        FreeboardPost savedPost = freeboardPostRepository.save(newPost);
        return convertToDetailDto(savedPost);
    }

    @Override
    @Transactional
    public FreeboardPostResponseDTO updatePost(Integer postId, FreeboardPostRequestDTO requestDto, String currentUserId /*, DetailUser principal - 역할 확인 필요시 */) { // <<< currentUserId 파라미터 추가
        FreeboardPost postToUpdate = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 게시글을 찾을 수 없습니다: " + postId));

        // User currentUser = userRepository.findUserByUserId(currentUserId).orElseThrow(...); // 필요시 현재 사용자 엔티티 조회
        // boolean isAdmin = principal != null && principal.getUser().getRole() == Role.ADMIN; // 예시: 관리자 여부 확인

        // 2. 수정 권한 확인: 요청한 사용자(currentUserId)와 실제 게시글 작성자의 userId가 일치하는지 확인
        //    또는 관리자(Admin)도 수정 가능하도록 로직 추가 가능
        if (!postToUpdate.getUser().getUserId().equals(currentUserId) /* && !isAdmin */ ) {
            throw new AccessDeniedException("게시글 수정 권한이 없습니다.");
        }

        postToUpdate.setPostTitle(requestDto.getPostTitle());
        postToUpdate.setPostContent(requestDto.getPostContent());
        return convertToDetailDto(postToUpdate);
    }

    @Override
    @Transactional
    public void deletePost(Integer postId, String currentUserId /*, DetailUser principal - 역할 확인 필요시 */) { // <<< requestUserId를 currentUserId로 변경
        FreeboardPost postToDelete = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다: " + postId));

        // User currentUser = userRepository.findUserByUserId(currentUserId).orElseThrow(...);
        // boolean isAdmin = principal != null && principal.getUser().getRole() == Role.ADMIN;

        // 2. 삭제 권한 확인
        if (!postToDelete.getUser().getUserId().equals(currentUserId) /* && !isAdmin */) {
            throw new AccessDeniedException("게시글 삭제 권한이 없습니다.");
        }

        freeboardPostRepository.delete(postToDelete);
    }

    @Override
    @Transactional
    public PostLikeResponseDTO togglePostLike(Integer postId, String currentUserId) { // <<< PostLikeRequestDTO requestDto 제거, currentUserId 파라미터 추가
        FreeboardPost post = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("좋아요를 누를 게시글을 찾을 수 없습니다: " + postId));

        // 사용자 조회 (이제 DTO가 아닌 currentUserId 사용)
        User user = userRepository.findUserByUserId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다: " + currentUserId));

        Optional<FreeboardPostLike> existingLike = freeboardPostLikeRepository.findByUserAndFreeboardPost(user, post);
        boolean likedByCurrentUser;

        if (existingLike.isPresent()) {
            freeboardPostLikeRepository.delete(existingLike.get());
            post.setPostLikeCount(Math.max(0, post.getPostLikeCount() - 1));
            likedByCurrentUser = false;
        } else {
            FreeboardPostLike newLike = FreeboardPostLike.builder()
                    .user(user)
                    .freeboardPost(post)
                    .build();
            freeboardPostLikeRepository.save(newLike);
            post.setPostLikeCount(post.getPostLikeCount() + 1);
            likedByCurrentUser = true;
        }

        return PostLikeResponseDTO.builder()
                .postId(post.getPostId())
                .currentLikeCount(post.getPostLikeCount())
                .likedByCurrentUser(likedByCurrentUser)
                .build();
    }

    @Override
    @Transactional
    public ReportSuccessResponseDTO reportPost(Integer postId, String currentUserId) { // <<< PostReportRequestDTO requestDto 제거, currentUserId 파라미터 추가
        FreeboardPost postToReport = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("신고할 게시글을 찾을 수 없습니다: " + postId));

        // 신고자 조회 (이제 DTO가 아닌 currentUserId 사용)
        User reporter = userRepository.findUserByUserId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("신고자 정보를 찾을 수 없습니다: " + currentUserId));

        if (postToReport.getUser().getUserId().equals(reporter.getUserId())) {
            throw new IllegalStateException("자신의 게시글은 신고할 수 없습니다.");
        }
        boolean alreadyReported = freeboardPostReportRepository.existsByUserAndFreeboardPost(reporter, postToReport);
        if (alreadyReported) {
            throw new IllegalStateException("이미 신고한 게시글입니다.");
        }

        FreeboardPostReport newReport = FreeboardPostReport.builder()
                .user(reporter)
                .freeboardPost(postToReport)
                .build();
        freeboardPostReportRepository.save(newReport);
        return new ReportSuccessResponseDTO("게시글이 성공적으로 신고되었습니다.", postId);
    }

    @Override
    public PageResponseDTO<ReportedPostEntryDTO> getReportedPosts(AdminReportedPostFilterDTO filter, Pageable pageable) {
        // 이 메소드는 관리자 기능이므로 currentUserId를 직접 받지 않습니다.
        // 접근 제어는 WebSecurityConfig 또는 컨트롤러의 @PreAuthorize 로 이루어집니다.
        AdminReportedPostFilterDTO queryFilter = new AdminReportedPostFilterDTO();
        queryFilter.setPostId(filter.getPostId());
        queryFilter.setAuthorUserId(filter.getAuthorUserId());
        queryFilter.setAuthorNickname(filter.getAuthorNickname());
        queryFilter.setPostTitle(filter.getPostTitle());
        queryFilter.setSearchKeyword(filter.getSearchKeyword());
        queryFilter.setIsHidden(filter.getIsHidden());

        if (filter.getPostStartDate() != null) {
            queryFilter.setQueryPostStartDate(filter.getPostStartDate().atStartOfDay());
        }
        if (filter.getPostEndDate() != null) {
            queryFilter.setQueryPostEndDate(filter.getPostEndDate().atTime(LocalTime.MAX));
        }

        Page<ReportedPostEntryDTO> reportedPostPage = freeboardPostReportRepository.findReportedPostSummariesWithFilters(queryFilter, pageable);

        return PageResponseDTO.<ReportedPostEntryDTO>builder()
                .content(reportedPostPage.getContent())
                .currentPage(reportedPostPage.getNumber() + 1)
                .totalPages(reportedPostPage.getTotalPages())
                .totalElements(reportedPostPage.getTotalElements())
                .size(reportedPostPage.getSize())
                .first(reportedPostPage.isFirst())
                .last(reportedPostPage.isLast())
                .empty(reportedPostPage.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public FreeboardPostResponseDTO updatePostVisibility(Integer postId, PostVisibilityRequestDTO requestDto) {
        // 이 메소드는 관리자 기능이므로 currentUserId를 직접 받지 않습니다.
        FreeboardPost post = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("상태를 변경할 게시글을 찾을 수 없습니다: " + postId));
        post.setPostIsHidden(requestDto.getIsHidden());
        return convertToDetailDto(post);
    }

    @Override
    public PageResponseDTO<FreeboardUserActivityItemDTO> getUserFreeboardActivity(String currentUserId, Pageable pageable) { // <<< userId를 currentUserId로 변경
        User user = userRepository.findUserByUserId(currentUserId) // currentUserId 사용
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다: " + currentUserId));

        List<FreeboardPost> userPosts = freeboardPostRepository.findByUserOrderByPostCreatedAtDesc(user);
        List<FreeboardComment> userComments = freeboardCommentRepository.findByUserOrderByCommentCreatedAtDesc(user);
        List<FreeboardUserActivityItemDTO> activities = new ArrayList<>();

        userPosts.forEach(post -> activities.add(
                FreeboardUserActivityItemDTO.builder()
                        .itemType("POST")
                        .itemId(post.getPostId())
                        .postTitle(post.getPostTitle())
                        .authorUserId(user.getUserId())
                        .authorNickname(user.getUserNickName())
                        .createdAt(post.getPostCreatedAt())
                        .likeCount(post.getPostLikeCount())
                        .viewCount(post.getPostViewCount())
                        .build()
        ));
        userComments.forEach(comment -> {
            String contentPreview = comment.getCommentContent();
            if (contentPreview != null && contentPreview.length() > 50) {
                contentPreview = contentPreview.substring(0, 50) + "...";
            }
            activities.add(
                    FreeboardUserActivityItemDTO.builder()
                            .itemType("COMMENT")
                            .itemId(comment.getCommentId())
                            .commentContentPreview(contentPreview)
                            .originalPostId(comment.getFreeboardPost().getPostId())
                            .originalPostTitle(comment.getFreeboardPost().getPostTitle())
                            .authorUserId(user.getUserId())
                            .authorNickname(user.getUserNickName())
                            .createdAt(comment.getCommentCreatedAt())
                            .likeCount(comment.getCommentLikeCount())
                            .build()
            );
        });

        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                Comparator<FreeboardUserActivityItemDTO> comparator = Comparator.comparing(
                        activity -> getComparableField(activity, order.getProperty()),
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
                if (order.getDirection().isDescending()) {
                    comparator = comparator.reversed();
                }
                activities.sort(comparator);
            }
        } else {
            activities.sort(Comparator.comparing(FreeboardUserActivityItemDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), activities.size());
        List<FreeboardUserActivityItemDTO> pageContent = List.of();
        if (start < end ) {
            pageContent = activities.subList(start, end);
        }
        Page<FreeboardUserActivityItemDTO> activityPage = new PageImpl<>(pageContent, pageable, activities.size());

        return PageResponseDTO.<FreeboardUserActivityItemDTO>builder()
                .content(activityPage.getContent())
                .currentPage(activityPage.getNumber() + 1)
                .totalPages(activityPage.getTotalPages())
                .totalElements(activityPage.getTotalElements())
                .size(activityPage.getSize())
                .first(activityPage.isFirst())
                .last(activityPage.isLast())
                .empty(activityPage.isEmpty())
                .build();
    }

    private Comparable getComparableField(FreeboardUserActivityItemDTO activity, String propertyName) {
        if (activity == null || propertyName == null) return null;
        try {
            switch (propertyName) {
                case "createdAt":
                    return activity.getCreatedAt();
                case "itemId":
                    return activity.getItemId();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private FreeboardPostSimpleResponseDTO convertToSimpleDto(FreeboardPost post) {
        User user = post.getUser();
        return FreeboardPostSimpleResponseDTO.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postViewCount(post.getPostViewCount())
                .postLikeCount(post.getPostLikeCount())
                .postCreatedAt(post.getPostCreatedAt())
                .userId(user != null ? user.getUserId() : null)
                .userNickName(user != null ? user.getUserNickName() : "알 수 없는 사용자")
                .build();
    }

    private FreeboardPostResponseDTO convertToDetailDto(FreeboardPost post) {
        User user = post.getUser();
        return FreeboardPostResponseDTO.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postContent(post.getPostContent())
                .postViewCount(post.getPostViewCount())
                .postLikeCount(post.getPostLikeCount())
                .postIsHidden(post.isPostIsHidden())
                .postCreatedAt(post.getPostCreatedAt())
                .postUpdatedAt(post.getPostUpdatedAt())
                .userId(user != null ? user.getUserId() : null)
                .userNickName(user != null ? user.getUserNickName() : "알 수 없는 사용자")
                .build();
    }
}