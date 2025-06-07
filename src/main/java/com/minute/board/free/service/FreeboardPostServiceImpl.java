package com.minute.board.free.service;

import com.minute.auth.service.DetailUser;
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
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j // <<< @Slf4j 어노테이션 추가
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreeboardPostServiceImpl implements FreeboardPostService {

    private final FreeboardPostRepository freeboardPostRepository;
    private final UserRepository userRepository;
    private final FreeboardPostLikeRepository freeboardPostLikeRepository;
    private final FreeboardPostReportRepository freeboardPostReportRepository;
    private final FreeboardCommentRepository freeboardCommentRepository;

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
    public PageResponseDTO<FreeboardPostSimpleResponseDTO> getAllPosts(
            Pageable pageable,
            @Nullable String authorUserId,
            @Nullable String searchKeyword,
            @Nullable LocalDate startDate,
            @Nullable LocalDate endDate) {

        String currentLoggedInUserId = getCurrentUserId();
        log.debug("[getAllPosts] Current logged in user ID: {}", currentLoggedInUserId != null ? currentLoggedInUserId : "null (guest)");

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
        List<FreeboardPost> posts = postPage.getContent();

        Set<Integer> likedPostIds = Collections.emptySet();
        Set<Integer> reportedPostIds = Collections.emptySet();

        if (currentLoggedInUserId != null && !posts.isEmpty()) {
            List<Integer> postIds = posts.stream().map(FreeboardPost::getPostId).collect(Collectors.toList());
            log.debug("[getAllPosts] Attempting to fetch liked/reported IDs for user {} on posts: {}", currentLoggedInUserId, postIds);

            likedPostIds = freeboardPostLikeRepository.findLikedPostIdsByUserIdAndPostIdsIn(currentLoggedInUserId, postIds);
            log.debug("[getAllPosts] Fetched likedPostIds from repository: {}", likedPostIds);

            reportedPostIds = freeboardPostReportRepository.findReportedPostIdsByUserIdAndPostIdsIn(currentLoggedInUserId, postIds);
            log.debug("[getAllPosts] Fetched reportedPostIds from repository: {}", reportedPostIds);
        }

        final Set<Integer> finalLikedPostIds = likedPostIds;
        final Set<Integer> finalReportedPostIds = reportedPostIds;

        List<FreeboardPostSimpleResponseDTO> dtoList = posts.stream()
                .map(post -> {
                    boolean isLiked = finalLikedPostIds.contains(post.getPostId());
                    boolean isReported = finalReportedPostIds.contains(post.getPostId());
                    log.debug("[getAllPosts] Post ID: {}, isLiked (for DTO): {}, isReported (for DTO): {}", post.getPostId(), isLiked, isReported);
                    return convertToSimpleDto(post, finalLikedPostIds, finalReportedPostIds);
                })
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
        FreeboardPost post = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + postId));

        post.setPostViewCount(post.getPostViewCount() + 1);

        boolean isLiked = false;
        boolean isReported = false;
        User currentUser = getCurrentUserEntity();

        if (currentUser != null) {
            log.debug("[getPostById] User {} is logged in. Checking like/report status for post {}.", currentUser.getUserId(), postId);
            isLiked = freeboardPostLikeRepository.existsByUserAndFreeboardPost(currentUser, post);
            log.debug("[getPostById] existsByUserAndFreeboardPost result for post {}: {}", postId, isLiked);

            isReported = freeboardPostReportRepository.existsByUserAndFreeboardPost(currentUser, post);
            log.debug("[getPostById] existsByUserAndFreeboardPost (report) result for post {}: {}", postId, isReported);
        }

        FreeboardPostResponseDTO dto = convertToDetailDto(post, isLiked, isReported);
        log.debug("[getPostById] Final DTO for post {}. isLikedByCurrentUser: {}, isReportedByCurrentUser: {}", post.getPostId(), dto.isLikedByCurrentUser(), dto.isReportedByCurrentUser());
        return dto;
    }

    @Override
    @Transactional
    public FreeboardPostResponseDTO createPost(FreeboardPostRequestDTO requestDto, String currentUserId) {
        User author = userRepository.findUserByUserId(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("작성자 정보를 찾을 수 없습니다: " + currentUserId));

        FreeboardPost newPost = FreeboardPost.builder()
                .postTitle(requestDto.getPostTitle())
                .postContent(requestDto.getPostContent())
                .user(author)
                .build();

        FreeboardPost savedPost = freeboardPostRepository.save(newPost);
        return convertToDetailDto(savedPost, false, false);
    }

    @Override
    @Transactional
    public FreeboardPostResponseDTO updatePost(Integer postId, FreeboardPostRequestDTO requestDto, String currentUserId) {
        FreeboardPost postToUpdate = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 게시글을 찾을 수 없습니다: " + postId));

        if (!postToUpdate.getUser().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("게시글 수정 권한이 없습니다.");
        }

        postToUpdate.setPostTitle(requestDto.getPostTitle());
        postToUpdate.setPostContent(requestDto.getPostContent());

        boolean isLiked = false;
        boolean isReported = false;
        User currentUser = getCurrentUserEntity();
        if (currentUser != null) {
            isLiked = freeboardPostLikeRepository.existsByUserAndFreeboardPost(currentUser, postToUpdate);
            isReported = freeboardPostReportRepository.existsByUserAndFreeboardPost(currentUser, postToUpdate);
        }
        return convertToDetailDto(postToUpdate, isLiked, isReported);
    }

    @Override
    @Transactional
    public void deletePost(Integer postId, String currentUserId) {
        FreeboardPost postToDelete = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다: " + postId));

        if (!postToDelete.getUser().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("게시글 삭제 권한이 없습니다.");
        }

        freeboardPostRepository.delete(postToDelete);
    }

    @Override
    @Transactional
    public PostLikeResponseDTO togglePostLike(Integer postId, String currentUserId) {
        FreeboardPost post = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("좋아요를 누를 게시글을 찾을 수 없습니다: " + postId));

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
    public ReportSuccessResponseDTO reportPost(Integer postId, String currentUserId) {
        FreeboardPost postToReport = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("신고할 게시글을 찾을 수 없습니다: " + postId));

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

        User reportedUser = postToReport.getUser();
        reportedUser.setUserReport(reportedUser.getUserReport() + 1);
        userRepository.save(reportedUser);

        return new ReportSuccessResponseDTO("게시글이 성공적으로 신고되었습니다.", postId);
    }

    @Override
    public PageResponseDTO<ReportedPostEntryDTO> getReportedPosts(AdminReportedPostFilterDTO filter, Pageable pageable) {
        System.out.println("Service getReportedPosts - Pageable Sort: " + pageable.getSort());
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
        FreeboardPost post = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("상태를 변경할 게시글을 찾을 수 없습니다: " + postId));
        post.setPostIsHidden(requestDto.getIsHidden());

        boolean isLiked = false;
        boolean isReported = false;
        User currentUser = getCurrentUserEntity();
        if (currentUser != null) {
            isLiked = freeboardPostLikeRepository.existsByUserAndFreeboardPost(currentUser, post);
            isReported = freeboardPostReportRepository.existsByUserAndFreeboardPost(currentUser, post);
        }
        return convertToDetailDto(post, isLiked, isReported);
    }

    @Override
    public PageResponseDTO<FreeboardUserActivityItemDTO> getUserFreeboardActivity(String currentUserId, Pageable pageable) {
        User user = userRepository.findUserByUserId(currentUserId)
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
            FreeboardPost originalPost = comment.getFreeboardPost();
            activities.add(
                    FreeboardUserActivityItemDTO.builder()
                            .itemType("COMMENT")
                            .itemId(comment.getCommentId())
                            .commentContentPreview(contentPreview)
                            .originalPostId(originalPost != null ? originalPost.getPostId() : null)
                            .originalPostTitle(originalPost != null ? originalPost.getPostTitle() : null)
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
        List<FreeboardUserActivityItemDTO> pageContent = (start <= end && start < activities.size()) ? activities.subList(start, end) : Collections.emptyList();
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
                case "createdAt": return activity.getCreatedAt();
                case "itemId": return activity.getItemId();
                default: return null;
            }
        } catch (Exception e) { return null; }
    }

    private FreeboardPostSimpleResponseDTO convertToSimpleDto(FreeboardPost post,
                                                              Set<Integer> likedPostIds,
                                                              Set<Integer> reportedPostIds) {
        User author = post.getUser();
        boolean isLiked = likedPostIds.contains(post.getPostId());
        boolean isReported = reportedPostIds.contains(post.getPostId());

        return FreeboardPostSimpleResponseDTO.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postViewCount(post.getPostViewCount())
                .postLikeCount(post.getPostLikeCount())
                .postCreatedAt(post.getPostCreatedAt())
                .userId(author != null ? author.getUserId() : null)
                .userNickName(author != null ? author.getUserNickName() : "알 수 없는 사용자")
                .isLikedByCurrentUser(isLiked)
                .isReportedByCurrentUser(isReported)
                .build();
    }

    private FreeboardPostResponseDTO convertToDetailDto(FreeboardPost post, boolean isLikedByCurrentUser, boolean isReportedByCurrentUser) {
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
                .isLikedByCurrentUser(isLikedByCurrentUser)
                .isReportedByCurrentUser(isReportedByCurrentUser)
                .build();
    }
}