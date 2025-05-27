package com.minute.board.free.service.admin; // 예시 패키지

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.free.dto.response.AdminReportedActivityItemDTO;
import com.minute.board.free.entity.FreeboardCommentReport;
import com.minute.board.free.entity.FreeboardPostReport;
import com.minute.board.free.repository.FreeboardCommentReportRepository;
import com.minute.board.free.repository.FreeboardPostRepository; // 게시글 정보 접근 시 필요
import com.minute.board.free.repository.FreeboardPostReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort; // Sort 사용

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportViewServiceImpl implements AdminReportViewService {

    private final FreeboardPostReportRepository freeboardPostReportRepository;
    private final FreeboardCommentReportRepository freeboardCommentReportRepository;
    // private final FreeboardPostRepository freeboardPostRepository; // 필요시 주입

    @Override
    public PageResponseDTO<AdminReportedActivityItemDTO> getAllReportedActivities(Pageable pageable) {
        // Pageable에서 정렬 정보를 확인하여 각 Repository 호출 시 적용하거나,
        // 메모리에서 정렬할 때 사용합니다. 기본은 신고일시 최신순으로 가정.
        // 여기서는 각 신고 목록을 가져와서 DTO로 변환 후 합치고 정렬하는 방식을 사용합니다.
        // **주의: 이 방식은 데이터가 매우 많을 경우 성능 문제가 발생할 수 있습니다.**
        // 프로덕션에서는 DB 레벨의 UNION ALL 또는 더 최적화된 접근 방식이 필요할 수 있습니다.

        // 1. 게시글 신고 목록 전체 조회 (페이징 없이, 또는 충분히 큰 사이즈로 - 이후 최적화 필요)
        //    정렬은 신고일시 기준으로 가져옵니다.
        List<FreeboardPostReport> postReports = freeboardPostReportRepository.findAll(Sort.by(Sort.Direction.DESC, "postReportDate"));

        // 2. 댓글 신고 목록 전체 조회 (페이징 없이, 또는 충분히 큰 사이즈로 - 이후 최적화 필요)
        List<FreeboardCommentReport> commentReports = freeboardCommentReportRepository.findAll(Sort.by(Sort.Direction.DESC, "commentReportDate"));

        List<AdminReportedActivityItemDTO> activities = new ArrayList<>();

        // 게시글 신고 내역을 DTO로 변환
        postReports.forEach(report -> {
            String preview = report.getFreeboardPost().getPostTitle(); // 게시글 제목
            if (preview != null && preview.length() > 50) {
                preview = preview.substring(0, 50) + "...";
            }
            activities.add(AdminReportedActivityItemDTO.builder()
                    .itemType("POST_REPORT")
                    .reportId(report.getPostReportId())
                    .reportedItemId(report.getFreeboardPost().getPostId())
                    .itemTitleOrContentPreview(preview)
                    .reportedItemAuthorUserId(report.getFreeboardPost().getUser().getUserId())
                    .reportedItemAuthorNickname(report.getFreeboardPost().getUser().getUserNickName())
                    .reporterUserId(report.getUser().getUserId())
                    .reporterNickname(report.getUser().getUserNickName())
                    .reportCreatedAt(report.getPostReportDate())
                    .originalItemCreatedAt(report.getFreeboardPost().getPostCreatedAt())
                    .isItemHidden(report.getFreeboardPost().isPostIsHidden())
                    .build());
        });

        // 댓글 신고 내역을 DTO로 변환
        commentReports.forEach(report -> {
            String preview = report.getFreeboardComment().getCommentContent();
            if (preview != null && preview.length() > 50) {
                preview = preview.substring(0, 50) + "...";
            }
            activities.add(AdminReportedActivityItemDTO.builder()
                    .itemType("COMMENT_REPORT")
                    .reportId(report.getCommentReportId())
                    .reportedItemId(report.getFreeboardComment().getCommentId())
                    .itemTitleOrContentPreview(preview)
                    .reportedItemAuthorUserId(report.getFreeboardComment().getUser().getUserId())
                    .reportedItemAuthorNickname(report.getFreeboardComment().getUser().getUserNickName())
                    .reporterUserId(report.getUser().getUserId())
                    .reporterNickname(report.getUser().getUserNickName())
                    .reportCreatedAt(report.getCommentReportDate())
                    .originalItemCreatedAt(report.getFreeboardComment().getCommentCreatedAt())
                    .isItemHidden(report.getFreeboardComment().isCommentIsHidden())
                    .originalPostIdForComment(report.getFreeboardComment().getFreeboardPost().getPostId())
                    .build());
        });

        // 신고일시(reportCreatedAt) 기준으로 최신순 정렬
        activities.sort(Comparator.comparing(AdminReportedActivityItemDTO::getReportCreatedAt).reversed());

        // 수동 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), activities.size());
        List<AdminReportedActivityItemDTO> pageContent = (start <= end && activities.size() > start) ? activities.subList(start, end) : List.of();

        Page<AdminReportedActivityItemDTO> activityPage = new PageImpl<>(pageContent, pageable, activities.size());

        return PageResponseDTO.<AdminReportedActivityItemDTO>builder()
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
}