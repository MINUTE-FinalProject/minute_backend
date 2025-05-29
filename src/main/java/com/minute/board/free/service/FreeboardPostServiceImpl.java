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
// import com.minute.user.enumpackage.Role; // Role은 여기서 직접 사용 안 함 (필요시 DetailUser 통해 확인)
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreeboardPostServiceImpl implements FreeboardPostService {

    private final FreeboardPostRepository freeboardPostRepository;
    private final UserRepository userRepository;
    private final FreeboardPostLikeRepository freeboardPostLikeRepository;
    private final FreeboardPostReportRepository freeboardPostReportRepository;
    private final FreeboardCommentRepository freeboardCommentRepository;

    // 현재 로그인한 사용자 ID를 가져오는 헬퍼 메서드 (중복될 수 있으므로 유틸리티 클래스로 분리 고려)
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

    // 현재 로그인한 User 엔티티를 가져오는 헬퍼 메서드
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

        String currentLoggedInUserId = getCurrentUserId(); // 현재 로그인 사용자 ID 가져오기

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
            likedPostIds = freeboardPostLikeRepository.findLikedPostIdsByUserIdAndPostIdsIn(currentLoggedInUserId, postIds);
            reportedPostIds = freeboardPostReportRepository.findReportedPostIdsByUserIdAndPostIdsIn(currentLoggedInUserId, postIds);
        }

        final Set<Integer> finalLikedPostIds = likedPostIds;
        final Set<Integer> finalReportedPostIds = reportedPostIds;

        List<FreeboardPostSimpleResponseDTO> dtoList = posts.stream()
                .map(post -> convertToSimpleDto(post, finalLikedPostIds, finalReportedPostIds))
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

        // 조회수 증가 (트랜잭션 내에서 이루어지므로 Dirty Checking에 의해 DB 반영)
        post.setPostViewCount(post.getPostViewCount() + 1);

        boolean isLiked = false;
        boolean isReported = false;
        User currentUser = getCurrentUserEntity(); // User 엔티티 직접 사용

        if (currentUser != null) {
            // FreeboardPostLikeRepository의 existsByUserAndFreeboardPost(User user, FreeboardPost post) 사용
            isLiked = freeboardPostLikeRepository.existsByUserAndFreeboardPost(currentUser, post);
            // FreeboardPostReportRepository의 existsByUserAndFreeboardPost(User user, FreeboardPost post) 사용
            isReported = freeboardPostReportRepository.existsByUserAndFreeboardPost(currentUser, post);
        }

        return convertToDetailDto(post, isLiked, isReported);
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
        // 새로 생성된 게시글은 현재 사용자가 좋아요/신고하지 않은 상태
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

        // 수정 시에는 기존 좋아요/신고 상태를 유지해야 하므로, 다시 조회
        boolean isLiked = false;
        boolean isReported = false;
        User currentUser = getCurrentUserEntity();
        if (currentUser != null) {
            isLiked = freeboardPostLikeRepository.existsByUserAndFreeboardPost(currentUser, postToUpdate);
            isReported = freeboardPostReportRepository.existsByUserAndFreeboardPost(currentUser, postToUpdate);
        }
        return convertToDetailDto(postToUpdate, isLiked, isReported);
    }

    // ... (deletePost, togglePostLike, reportPost, getReportedPosts 등 나머지 메서드는 이전과 거의 동일하게 유지) ...
    // (단, togglePostLike, reportPost의 반환값 업데이트 시 post 엔티티의 isLiked/ReportedByCurrentUser는 없으므로 DTO만 정확히 반환)

    @Override
    @Transactional
    public void deletePost(Integer postId, String currentUserId /*, DetailUser principal - 역할 확인 필요시 */) {
        FreeboardPost postToDelete = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다: " + postId));

        // boolean isAdmin = ... (updatePost와 유사하게 역할 확인 가능)

        if (!postToDelete.getUser().getUserId().equals(currentUserId) /* && !isAdmin */) {
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
        // post 엔티티의 변경사항(postLikeCount)은 @Transactional에 의해 커밋 시점에 DB에 반영됨

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
        return new ReportSuccessResponseDTO("게시글이 성공적으로 신고되었습니다.", postId);
    }

    @Override
    public PageResponseDTO<ReportedPostEntryDTO> getReportedPosts(AdminReportedPostFilterDTO filter, Pageable pageable) {
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

        // 숨김 상태 변경 시에도 좋아요/신고 상태는 유지되어야 함
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
        // ... (이전과 동일한 로직, 필요시 내부 DTO 빌드 시 isLikedByCurrentUser 등 추가 고려 가능하나 현재 DTO 스펙에는 없음) ...
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

    // convertToDetailDto 시그니처 변경 및 로직 수정
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
                .isLikedByCurrentUser(isLikedByCurrentUser) // 필드 설정
                .isReportedByCurrentUser(isReportedByCurrentUser) // 필드 설정
                .build();
    }
}