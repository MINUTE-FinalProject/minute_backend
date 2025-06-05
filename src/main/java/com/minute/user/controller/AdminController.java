package com.minute.user.controller;

import com.minute.auth.dto.response.ResponseDto;
import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.free.dto.request.AdminReportedCommentFilterDTO;
import com.minute.board.free.dto.request.AdminReportedPostFilterDTO;
import com.minute.board.free.dto.response.AdminReportedCommentEntryDTO;
import com.minute.board.free.dto.response.ReportedPostEntryDTO;
import com.minute.board.free.service.FreeboardCommentService;
import com.minute.board.free.service.FreeboardPostService;
import com.minute.board.free.service.admin.AdminReportViewService;
import com.minute.user.dto.request.MemberReportsSumaryDto;
import com.minute.user.service.UserService;
import com.minute.user.service.implement.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserServiceImpl userServiceImpl;
    private final UserService userService;
    private final FreeboardPostService freeboardPostService;
    private final FreeboardCommentService freeboardCommentService;


    @PatchMapping("/promote/{userId}")
    public ResponseEntity<?> promoteUserToAdmin(@PathVariable String userId) {
        userServiceImpl.promoteUserToAdmin(userId);
        return ResponseEntity.ok("관리자 승격 완료");
    }

    @PatchMapping("/status/{userId}")
    public ResponseEntity<?> changeStatus(@PathVariable String userId) {
        userServiceImpl.changeStatus(userId);
        return ResponseEntity.ok("회원 상태 변경 완료.");
    }

    //회원 삭제
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<? super ResponseDto> deleteUserByAdmin(@PathVariable String userId) {
        return userService.deleteUser(userId);
    }

    @Operation(summary = "[관리자] 특정 회원의 신고된 게시글/댓글 전체 조회", description = "회원 ID를 기준으로 신고된 게시글과 댓글을 함께 조회합니다. (관리자용)")
    @GetMapping("/reports/member/{userId}")
    public ResponseEntity<MemberReportsSumaryDto> getMemberReportedContent(
            @PathVariable String userId,
            @PageableDefault(size = 10) Pageable pageable
            /* @AuthenticationPrincipal DetailUser adminPrincipal */) {

        // 게시글 필터
        AdminReportedPostFilterDTO postFilter = new AdminReportedPostFilterDTO();
        postFilter.setAuthorUserId(userId);
        PageResponseDTO<ReportedPostEntryDTO> reportedPosts = freeboardPostService.getReportedPosts(postFilter, pageable);

        // 댓글 필터
        AdminReportedCommentFilterDTO commentFilter = new AdminReportedCommentFilterDTO();
        commentFilter.setAuthorUserId(userId);
        PageResponseDTO<AdminReportedCommentEntryDTO> reportedComments = freeboardCommentService.getReportedComments(commentFilter, pageable);

        // 통합 응답 생성
        MemberReportsSumaryDto summary = new MemberReportsSumaryDto(reportedPosts, reportedComments);
        return ResponseEntity.ok(summary);
    }

}
