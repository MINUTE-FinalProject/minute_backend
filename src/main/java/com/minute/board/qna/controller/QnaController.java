package com.minute.board.qna.controller;

import com.minute.board.qna.dto.request.QnaCreateRequestDTO;
import com.minute.board.qna.dto.request.QnaUpdateRequestDTO; // 추가
import com.minute.board.qna.dto.response.QnaDetailResponseDTO;
import com.minute.board.qna.dto.response.QnaSummaryResponseDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/qna")
@RequiredArgsConstructor
@Tag(name = "01. QnA (User)", description = "사용자 문의(Q&A) 관련 API")
@SecurityRequirement(name = "bearerAuth") // Swagger UI에서 JWT 인증 필요 명시
public class QnaController {

    private final QnaService qnaService;

    @Operation(summary = "새 문의 작성", description = "사용자가 새로운 문의를 제목, 내용, 첨부파일과 함께 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "문의 작성 성공",
                    content = @Content(schema = @Schema(implementation = QnaDetailResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (필수값 누락, 형식 오류 등)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 (파일 업로드 실패 등)")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // 파일 업로드를 위해 multipart/form-data 사용
    public ResponseEntity<QnaDetailResponseDTO> createQna(
            @Parameter(description = "문의 내용 DTO (JSON 형식)", schema = @Schema(type = "string", format = "binary"))
            @Valid @RequestPart("qnaCreateRequest") QnaCreateRequestDTO requestDTO, // 프론트에서 'qnaCreateRequest' key로 JSON 데이터 전송
            @Parameter(description = "첨부 파일 (이미지 등, 최대 3개)")
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) throws IOException { // 인증된 사용자 정보

        if (authentication == null || !authentication.isAuthenticated()) {
            // Spring Security에서 처리되지만, 명시적으로 방어 코드 추가 가능
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userId = authentication.getName(); // JwtAuthenticationFilter에서 userId를 Principal로 설정한 경우
        log.info("Creating QnA for user: {}", userId);

        QnaDetailResponseDTO createdQna = qnaService.createQna(requestDTO, files, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQna);
    }

    @Operation(summary = "내 문의 목록 조회", description = "현재 로그인한 사용자의 문의 목록을 페이징 및 검색 조건과 함께 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "페이지 당 항목 수", example = "10", in = ParameterIn.QUERY),
            @Parameter(name = "sort", description = "정렬 조건 (예: inquiryCreatedAt,desc)", example = "inquiryCreatedAt,desc", in = ParameterIn.QUERY),
            @Parameter(name = "searchTerm", description = "검색어 (제목 또는 내용)", example = "결제", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<Page<QnaSummaryResponseDTO>> getMyQnas(
            @PageableDefault(size = 10, sort = "inquiryCreatedAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchTerm,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userId = authentication.getName();
        log.info("Fetching QnAs for user: {}, page: {}, size: {}, search: {}", userId, pageable.getPageNumber(), pageable.getPageSize(), searchTerm);

        Page<QnaSummaryResponseDTO> qnaPage = qnaService.getMyQnas(userId, pageable, searchTerm);
        return ResponseEntity.ok(qnaPage);
    }

    @Operation(summary = "내 문의 상세 조회", description = "현재 로그인한 사용자의 특정 문의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (본인 문의가 아님)"),
            @ApiResponse(responseCode = "404", description = "해당 문의를 찾을 수 없음")
    })
    @GetMapping("/{qnaId}")
    public ResponseEntity<QnaDetailResponseDTO> getMyQnaDetail(
            @Parameter(description = "조회할 문의 ID", required = true, example = "1") @PathVariable Integer qnaId,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userId = authentication.getName();
        log.info("Fetching QnA detail for qnaId: {}, user: {}", qnaId, userId);

        QnaDetailResponseDTO qnaDetail = qnaService.getMyQnaDetail(qnaId, userId);
        return ResponseEntity.ok(qnaDetail);
    }

    // --- 사용자 문의 수정/삭제 엔드포인트 (새로 추가) ---

    @Operation(summary = "내 문의 수정", description = "사용자가 자신이 작성한 문의를 수정합니다. (제목, 내용, 첨부파일 변경)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 수정 성공",
                    content = @Content(schema = @Schema(implementation = QnaDetailResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (본인 문의가 아님)"),
            @ApiResponse(responseCode = "404", description = "수정할 문의를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 (파일 처리 실패 등)")
    })
    @PutMapping(value = "/{qnaId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QnaDetailResponseDTO> updateMyQna(
            @Parameter(description = "수정할 문의 ID", required = true, example = "1") @PathVariable Integer qnaId,
            @Parameter(description = "수정할 문의 내용 DTO (JSON 형식)", schema = @Schema(type = "string", format = "binary"))
            @Valid @RequestPart("qnaUpdateRequest") QnaUpdateRequestDTO requestDTO, // 프론트에서 'qnaUpdateRequest' key로 JSON 데이터 전송
            @Parameter(description = "새로 첨부할 파일 목록 (기존 파일은 qnaUpdateRequest.attachmentIdsToDelete 로 삭제 지정)")
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles,
            Authentication authentication) throws IOException {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userId = authentication.getName();
        log.info("User {} updating QnA ID: {}", userId, qnaId);

        QnaDetailResponseDTO updatedQna = qnaService.updateMyQna(qnaId, requestDTO, newFiles, userId);
        return ResponseEntity.ok(updatedQna);
    }

    @Operation(summary = "내 문의 삭제", description = "사용자가 자신이 작성한 문의를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "문의 삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (본인 문의가 아님)"),
            @ApiResponse(responseCode = "404", description = "삭제할 문의를 찾을 수 없음")
    })
    @DeleteMapping("/{qnaId}")
    public ResponseEntity<Void> deleteMyQna(
            @Parameter(description = "삭제할 문의 ID", required = true, example = "1") @PathVariable Integer qnaId,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userId = authentication.getName();
        log.info("User {} deleting QnA ID: {}", userId, qnaId);

        qnaService.deleteMyQna(qnaId, userId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}