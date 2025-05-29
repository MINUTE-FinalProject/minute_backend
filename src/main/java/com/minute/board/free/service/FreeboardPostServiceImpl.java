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
// import com.minute.user.enumpackage.Role; // Role enum은 이 파일에서 직접 사용되지 않으면 제거 가능
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
import java.util.Collections; // Collections.emptySet() 사용을 위해 추가
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set; // Set import 추가
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

        String currentUserId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String &&
                        authentication.getPrincipal().equals("anonymousUser"))) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof DetailUser) {
                DetailUser detailUser = (DetailUser) principal;
                if (detailUser.getUser() != null && detailUser.getUser().getUserId() != null) {
                    currentUserId = detailUser.getUser().getUserId();
                }
            }
        }

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

        if (currentUserId != null && !posts.isEmpty()) {
            List<Integer> postIds = posts.stream().map(FreeboardPost::getPostId).collect(Collectors.toList());
            likedPostIds = freeboardPostLikeRepository.findLikedPostIdsByUserIdAndPostIdsIn(currentUserId, postIds);
            reportedPostIds = freeboardPostReportRepository.findReportedPostIdsByUserIdAndPostIdsIn(currentUserId, postIds);
        }

        // final 변수로 만들어 람다에서 사용
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
        post.setPostViewCount(post.getPostViewCount() + 1);
        // 상세 조회 시에도 좋아요/신고 여부를 보여주고 싶다면, 이 부분도 currentUserId를 가져와 처리해야 합니다.
        // 여기서는 일단 기존 로직을 유지합니다.
        return convertToDetailDto(post);
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
        return convertToDetailDto(savedPost);
    }

    @Override
    @Transactional
    public FreeboardPostResponseDTO updatePost(Integer postId, FreeboardPostRequestDTO requestDto, String currentUserId /*, DetailUser principal - 역할 확인 필요시 */) {
        FreeboardPost postToUpdate = freeboardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 게시글을 찾을 수 없습니다: " + postId));

        // 현재 로그인한 사용자의 Role 정보를 가져오려면 principal 객체가 필요합니다.
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // boolean isAdmin = false;
        // if (authentication != null && authentication.getPrincipal() instanceof DetailUser) {
        //     DetailUser detailUser = (DetailUser) authentication.getPrincipal();
        //     isAdmin = detailUser.getAuthorities().stream()
        //                     .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")); // 또는 "ADMIN" - Role 설정에 따라
        // }


        if (!postToUpdate.getUser().getUserId().equals(currentUserId) /* && !isAdmin */ ) {
            throw new AccessDeniedException("게시글 수정 권한이 없습니다.");
        }

        postToUpdate.setPostTitle(requestDto.getPostTitle());
        postToUpdate.setPostContent(requestDto.getPostContent());
        return convertToDetailDto(postToUpdate);
    }

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
            post.setPostLikeCount(Math.max(0, post.getPostLikeCount() - 1)); // 좋아요 수 직접 업데이트
            likedByCurrentUser = false;
        } else {
            FreeboardPostLike newLike = FreeboardPostLike.builder()
                    .user(user)
                    .freeboardPost(post)
                    .build();
            freeboardPostLikeRepository.save(newLike);
            post.setPostLikeCount(post.getPostLikeCount() + 1); // 좋아요 수 직접 업데이트
            likedByCurrentUser = true;
        }
        // freeboardPostRepository.save(post); // 변경된 좋아요 수를 post 엔티티에 반영 (만약 likeCount가 집계 필드가 아닌 직접 관리 필드라면)
        // 현재는 FreeboardPost 엔티티의 postLikeCount 필드를 직접 수정하므로,
        // 이 @Transactional 메서드가 종료될 때 post 엔티티의 변경 사항이 DB에 반영됩니다.
        // 따라서 명시적인 save 호출은 필요 없을 수 있습니다. (JPA Dirty Checking)

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
        return convertToDetailDto(post);
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
            FreeboardPost originalPost = comment.getFreeboardPost(); // Null check for originalPost
            activities.add(
                    FreeboardUserActivityItemDTO.builder()
                            .itemType("COMMENT")
                            .itemId(comment.getCommentId())
                            .commentContentPreview(contentPreview)
                            .originalPostId(originalPost != null ? originalPost.getPostId() : null) // Null safe access
                            .originalPostTitle(originalPost != null ? originalPost.getPostTitle() : null) // Null safe access
                            .authorUserId(user.getUserId())
                            .authorNickname(user.getUserNickName())
                            .createdAt(comment.getCommentCreatedAt())
                            .likeCount(comment.getCommentLikeCount())
                            .build()
            );
        });

        // Sort activities
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
            // Default sort: newest first
            activities.sort(Comparator.comparing(FreeboardUserActivityItemDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        // Manual pagination
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
                // Add other sortable properties if needed
                default: return null;
            }
        } catch (Exception e) {
            // Log error or handle as appropriate
            return null;
        }
    }

    // <<< convertToSimpleDto 메소드 시그니처 및 내용 수정 (N+1 해결) >>>
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