package com.minute.board.qna.controller;

import com.minute.board.qna.dto.request.QnaReplyRequestDTO;
import com.minute.board.qna.dto.response.AdminQnaDetailResponseDTO;
import com.minute.board.qna.dto.response.AdminQnaSummaryResponseDTO;
import com.minute.board.qna.dto.response.QnaReplyResponseDTO;
import com.minute.board.qna.service.QnaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.minute.board.qna.dto.response.QnaReportResponseDTO; // 추가

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/qna") // 관리자용 QnA API 경로
@RequiredArgsConstructor
@Tag(name = "02. QnA (Admin)", description = "관리자용 문의(Q&A) 관리 API")
@SecurityRequirement(name = "bearerAuth") // Swagger UI에서 JWT 인증 필요 명시
public class AdminQnaController {

    private final QnaService qnaService; // QnaServiceImpl 주입

    @Operation(summary = "전체 문의 목록 조회 (관리자용)", description = "관리자가 모든 문의 목록을 페이징, 검색, 필터 조건과 함께 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "페이지 당 항목 수", example = "10", in = ParameterIn.QUERY),
            @Parameter(name = "sort", description = "정렬 조건 (예: inquiryCreatedAt,desc)", example = "inquiryCreatedAt,desc", in = ParameterIn.QUERY),
            @Parameter(name = "searchTerm", description = "검색어 (제목, 내용, 작성자ID, 닉네임)", example = "오류", in = ParameterIn.QUERY),
            @Parameter(name = "statusFilter", description = "답변 상태 필터 (PENDING, ANSWERED)", example = "PENDING", in = ParameterIn.QUERY),
            @Parameter(name = "startDate", description = "검색 시작일 (YYYY-MM-DD)", example = "2024-01-01", in = ParameterIn.QUERY),
            @Parameter(name = "endDate", description = "검색 종료일 (YYYY-MM-DD)", example = "2024-12-31", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음 (토큰 누락 또는 만료)"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (관리자 아님)")
    })
    @GetMapping
    public ResponseEntity<Page<AdminQnaSummaryResponseDTO>> getAllQnasForAdmin(
            @PageableDefault(size = 10, sort = "inquiryCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) { // 관리자 권한 확인은 Spring Security에서 처리

        log.info("Admin request: Get all QnAs. Filters - Search: '{}', Status: '{}', Start: {}, End: {}",
                searchTerm, statusFilter, startDate, endDate);
        Page<AdminQnaSummaryResponseDTO> qnaPage = qnaService.getAllQnasForAdmin(pageable, searchTerm, statusFilter, startDate, endDate);
        return ResponseEntity.ok(qnaPage);
    }

    @Operation(summary = "문의 상세 조회 (관리자용)", description = "관리자가 특정 문의의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 문의를 찾을 수 없음")
    })
    @GetMapping("/{qnaId}")
    public ResponseEntity<AdminQnaDetailResponseDTO> getQnaDetailForAdmin(
            @Parameter(description = "조회할 문의 ID", required = true, example = "1") @PathVariable Integer qnaId,
            Authentication authentication) {

        log.info("Admin request: Get QnA detail for qnaId: {}", qnaId);
        AdminQnaDetailResponseDTO qnaDetail = qnaService.getQnaDetailForAdmin(qnaId);
        return ResponseEntity.ok(qnaDetail);
    }

    @Operation(summary = "문의 답변 작성 (관리자용)", description = "관리자가 특정 문의에 답변을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "답변 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (내용 누락 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "답변할 문의를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 답변이 존재함 (정책에 따라)")
    })
    @PostMapping("/{qnaId}/replies")
    public ResponseEntity<QnaReplyResponseDTO> createReplyToQna(
            @Parameter(description = "답변을 작성할 문의 ID", required = true, example = "1") @PathVariable Integer qnaId,
            @Valid @RequestBody QnaReplyRequestDTO requestDTO,
            Authentication authentication) {

        String adminUserId = authentication.getName(); // 관리자 ID (Principal)
        log.info("Admin request: Create reply for qnaId: {} by admin: {}", qnaId, adminUserId);

        QnaReplyResponseDTO createdReply = qnaService.createReplyToQna(qnaId, requestDTO, adminUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReply);
    }

    // --- 관리자 답변 수정/삭제 엔드포인트 (새로 추가) ---

    @Operation(summary = "문의 답변 수정 (관리자용)", description = "관리자가 특정 문의 답변을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (내용 누락 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "수정할 답변을 찾을 수 없음")
    })
    @PutMapping("/replies/{replyId}") // 경로를 /replies/{replyId}로 명확히 함
    public ResponseEntity<QnaReplyResponseDTO> updateAdminReply(
            @Parameter(description = "수정할 답변 ID", required = true, example = "1") @PathVariable Integer replyId,
            @Valid @RequestBody QnaReplyRequestDTO requestDTO,
            Authentication authentication) {

        String adminUserId = authentication.getName();
        log.info("Admin request: Update reply ID: {} by admin: {}", replyId, adminUserId);

        QnaReplyResponseDTO updatedReply = qnaService.updateAdminReply(replyId, requestDTO, adminUserId);
        return ResponseEntity.ok(updatedReply);
    }

    @Operation(summary = "문의 답변 삭제 (관리자용)", description = "관리자가 특정 문의 답변을 삭제합니다. 삭제 시 원본 문의는 'PENDING' 상태로 변경됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "답변 삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "삭제할 답변을 찾을 수 없음")
    })
    @DeleteMapping("/replies/{replyId}") // 경로를 /replies/{replyId}로 명확히 함
    public ResponseEntity<Void> deleteAdminReply(
            @Parameter(description = "삭제할 답변 ID", required = true, example = "1") @PathVariable Integer replyId,
            Authentication authentication) {

        String adminUserId = authentication.getName();
        log.info("Admin request: Delete reply ID: {} by admin: {}", replyId, adminUserId);

        qnaService.deleteAdminReply(replyId, adminUserId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    // --- 관리자 QnA 신고 생성 엔드포인트 (새로 추가) ---
    @Operation(summary = "문의에 대한 관리자 신고 생성", description = "관리자가 특정 문의(QnA)에 대해 신고(QnaReport)를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "관리자 신고 성공적으로 생성됨",
                    content = @Content(schema = @Schema(implementation = QnaReportResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않음"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (관리자 아님)"),
            @ApiResponse(responseCode = "404", description = "신고할 문의 또는 관리자 계정을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 해당 관리자가 신고한 문의 (Conflict)")
    })
    @PostMapping("/{qnaId}/reports") // 특정 QnA에 대한 'reports' 리소스 생성으로 해석
    public ResponseEntity<QnaReportResponseDTO> createQnaReportByAdmin(
            @Parameter(description = "신고할 문의(QnA)의 ID", required = true, example = "1") @PathVariable Integer qnaId,
            Authentication authentication) {

        String adminUserId = authentication.getName(); // 관리자 ID (Principal)
        log.info("Admin request: Admin {} creating report for QnA ID: {}", adminUserId, qnaId);

        // 서비스 호출: 성공 시 QnaReportResponseDTO 반환, 이미 신고했거나 문제 발생 시 예외 발생
        // (QnaServiceImpl에서 EntityNotFoundException 또는 IllegalStateException 등을 던지도록 구현)
        QnaReportResponseDTO reportResponse = qnaService.createQnaReportByAdmin(qnaId, adminUserId);

        // 성공적으로 새로운 신고가 생성된 경우
        return ResponseEntity.status(HttpStatus.CREATED).body(reportResponse);
    }
}