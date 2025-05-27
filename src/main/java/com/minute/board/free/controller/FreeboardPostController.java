package com.minute.board.free.controller; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.common.dto.response.ReportSuccessResponseDTO;
import com.minute.board.free.dto.request.*;
import com.minute.board.free.dto.response.*;
import com.minute.board.free.service.FreeboardCommentService;
import com.minute.board.free.service.FreeboardPostService;
import com.minute.board.free.service.admin.AdminReportViewService;
import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@Tag(name = "01. 자유게시판 API", description = "자유게시판 게시글 관련 API입니다.") // API 그룹 태그
@RestController
@RequestMapping("/api/v1/board/free") // 공통 경로
@RequiredArgsConstructor
public class FreeboardPostController {

    private final FreeboardPostService freeboardPostService;
    private final FreeboardCommentService freeboardCommentService; // FreeboardCommentService 주입
    private final AdminReportViewService adminReportViewService; // <<< AdminReportViewService 주입

    @Operation(summary = "자유게시판 게시글 목록 조회", description = "페이징 처리된 자유게시판 게시글 목록을 조회합니다. 다양한 필터(작성자, 검색어, 날짜 범위)를 적용할 수 있습니다.")
    @ApiResponses(value = { /* ... */ })
    @Parameters({
            @Parameter(name = "page", description = "요청할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "size", description = "한 페이지에 보여줄 게시글 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "sort", description = "정렬 조건 (예: postId,desc 또는 postCreatedAt,asc).", example = "postId,desc", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "authorUserId", description = "조회할 작성자의 User ID (선택 사항)", example = "wansu00", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "searchKeyword", description = "검색할 키워드 (제목, 내용, 닉네임 통합 검색, 선택 사항)", example = "오늘", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "startDate", description = "검색 시작일 (YYYY-MM-DD 형식, 선택 사항)", example = "2025-01-01", in = ParameterIn.QUERY, schema = @Schema(type = "string", format = "date")),
            @Parameter(name = "endDate", description = "검색 종료일 (YYYY-MM-DD 형식, 선택 사항)", example = "2025-01-31", in = ParameterIn.QUERY, schema = @Schema(type = "string", format = "date"))
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<FreeboardPostSimpleResponseDTO>> getAllPosts(
            @PageableDefault(size = 10, sort = "postId", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) @Nullable String authorUserId,
            @RequestParam(required = false) @Nullable String searchKeyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nullable LocalDate endDate) {

        PageResponseDTO<FreeboardPostSimpleResponseDTO> response = freeboardPostService.getAllPosts(pageable, authorUserId, searchKeyword, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // 여기에 게시글 상세 조회, 작성, 수정, 삭제 등 다른 API 엔드포인트들이 추가될 예정입니다.

    @Operation(summary = "자유게시판 게시글 상세 조회", description = "특정 ID의 게시글 상세 정보를 조회하고, 조회수를 1 증가시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = FreeboardPostResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 ID에 해당하는 게시글을 찾을 수 없음 (메시지는 GlobalExceptionHandler 또는 여기서 직접 설정 가능)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 처리 오류")
    })
    @Parameter(name = "postId", description = "조회할 게시글의 고유 ID", required = true, example = "1", in = ParameterIn.PATH, schema = @Schema(type = "integer"))
    @GetMapping("/{postId}")
    public ResponseEntity<FreeboardPostResponseDTO> getPostById(@PathVariable Integer postId) {
        FreeboardPostResponseDTO responseDto = freeboardPostService.getPostById(postId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "자유게시판 게시글 작성", description = "새로운 자유게시판 게시글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 작성 성공",
                    content = @Content(schema = @Schema(implementation = FreeboardPostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 제목/내용 누락, 사용자 ID 없음 등)"),
            @ApiResponse(responseCode = "404", description = "요청 DTO의 userId에 해당하는 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 처리 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody( // Swagger UI에서 RequestBody 명시
            description = "생성할 게시글의 정보와 작성자 ID를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = FreeboardPostRequestDTO.class))
    )
    @PostMapping
    public ResponseEntity<FreeboardPostResponseDTO> createPost(
            @Valid @org.springframework.web.bind.annotation.RequestBody FreeboardPostRequestDTO requestDto) { // @Valid로 DTO 유효성 검사

        FreeboardPostResponseDTO responseDto = freeboardPostService.createPost(requestDto);

        // 생성된 리소스의 URI를 Location 헤더에 포함하여 반환 (RESTful API 권장 사항)
        URI location = ServletUriComponentsBuilder.fromCurrentRequest() // 현재 요청 URI (/api/v1/board/free)
                .path("/{id}") // 여기에 생성된 리소스의 ID를 추가
                .buildAndExpand(responseDto.getPostId()) // responseDto에서 postId를 가져와 {id}에 바인딩
                .toUri();

        return ResponseEntity.created(location).body(responseDto); // HTTP 201 Created
    }

    @Operation(summary = "자유게시판 게시글 수정", description = "기존 자유게시판 게시글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
                    content = @Content(schema = @Schema(implementation = FreeboardPostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 제목/내용 누락 등)"),
            @ApiResponse(responseCode = "403", description = "게시글 수정 권한 없음"), // AccessDeniedException 발생 시
            @ApiResponse(responseCode = "404", description = "수정할 게시글을 찾을 수 없거나, 요청 DTO의 userId에 해당하는 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 처리 오류")
    })
    @Parameter(name = "postId", description = "수정할 게시글의 고유 ID", required = true, example = "1", in = ParameterIn.PATH, schema = @Schema(type = "integer"))
    @io.swagger.v3.oas.annotations.parameters.RequestBody( // Swagger UI에서 RequestBody 명시
            description = "수정할 게시글의 정보와 수정을 시도하는 사용자의 ID를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = FreeboardPostRequestDTO.class))
    )
    @PutMapping("/{postId}")
    public ResponseEntity<FreeboardPostResponseDTO> updatePost(
            @PathVariable Integer postId,
            @Valid @org.springframework.web.bind.annotation.RequestBody FreeboardPostRequestDTO requestDto) {

        FreeboardPostResponseDTO responseDto = freeboardPostService.updatePost(postId, requestDto);
        return ResponseEntity.ok(responseDto); // HTTP 200 OK
    }

    @Operation(summary = "자유게시판 게시글 삭제", description = "특정 ID의 게시글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "게시글 삭제 성공 (No Content)"), // 또는 200 OK 와 함께 메시지 DTO
            @ApiResponse(responseCode = "403", description = "게시글 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "삭제할 게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 처리 오류")
    })
    @Parameters({
            @Parameter(name = "postId", description = "삭제할 게시글의 고유 ID", required = true, example = "1", in = ParameterIn.PATH, schema = @Schema(type = "integer")),
            @Parameter(name = "userId", description = "삭제를 요청하는 사용자의 ID (임시 권한 확인용)", required = true, example = "wansu00", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Integer postId,
            @RequestParam String userId) { // 임시 권한 확인을 위해 요청 파라미터로 userId 받음

        freeboardPostService.deletePost(postId, userId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
        // 또는 return ResponseEntity.ok().body(new MessageResponseDto("게시글이 성공적으로 삭제되었습니다.")); 와 같이 메시지 반환 가능
    }

    @Operation(summary = "특정 게시글의 댓글 목록 조회", description = "페이징 처리된 댓글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponseDTO.class))), // 실제로는 PageResponseDTO<FreeboardCommentResponseDTO>
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음 (구현 시 추가 가능)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters({
            @Parameter(name = "postId", description = "댓글을 조회할 게시글의 ID", required = true, example = "1", in = ParameterIn.PATH, schema = @Schema(type = "integer")),
            @Parameter(name = "page", description = "요청할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "size", description = "한 페이지에 보여줄 댓글 수", example = "5", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "sort", description = "정렬 조건 (예: commentId,asc 또는 commentCreatedAt,desc)", example = "commentCreatedAt,asc", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @GetMapping("/{postId}/comments")
    public ResponseEntity<PageResponseDTO<FreeboardCommentResponseDTO>> getCommentsByPostId(
            @PathVariable Integer postId,
            @PageableDefault(size = 5, sort = "commentCreatedAt", direction = Sort.Direction.ASC) Pageable pageable) {

        PageResponseDTO<FreeboardCommentResponseDTO> response = freeboardCommentService.getCommentsByPostId(postId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 게시글에 댓글 작성", description = "새로운 댓글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 작성 성공",
                    content = @Content(schema = @Schema(implementation = FreeboardCommentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 내용 누락 등)"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 작성자 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody( // Swagger UI에서 RequestBody 명시
            description = "생성할 댓글의 내용과 작성자 ID를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = FreeboardCommentRequestDTO.class))
    )
    @PostMapping("/{postId}/comments")
    public ResponseEntity<FreeboardCommentResponseDTO> createComment(
            @Parameter(description = "댓글을 작성할 게시글의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer postId,
            @Valid @org.springframework.web.bind.annotation.RequestBody FreeboardCommentRequestDTO requestDto) {

        FreeboardCommentResponseDTO responseDto = freeboardCommentService.createComment(postId, requestDto);

        // 생성된 댓글 리소스의 URI (단일 댓글 조회 API가 있다면 그곳으로, 없다면 댓글 목록 또는 게시글 상세로)
        // 여기서는 댓글 목록을 가리키도록 예시
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/board/free/{postId}/comments") // 댓글 목록 경로
                .queryParam("commentId", responseDto.getCommentId()) // 생성된 댓글 ID를 쿼리 파라미터로 추가 (선택적)
                .buildAndExpand(postId)
                .toUri();
        // 단일 댓글 조회 API가 있다면: .path("/api/v1/board/free/comments/{commentId}").buildAndExpand(responseDto.getCommentId()).toUri();

        return ResponseEntity.created(location).body(responseDto); // HTTP 201 Created
    }

    @Operation(summary = "댓글 수정", description = "기존 댓글의 내용을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
                    content = @Content(schema = @Schema(implementation = FreeboardCommentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 내용 누락 등)"),
            @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "수정할 댓글을 찾을 수 없거나, 요청 DTO의 userId에 해당하는 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 댓글의 내용과 수정을 시도하는 사용자의 ID를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = FreeboardCommentRequestDTO.class))
    )
    @PutMapping("/comments/{commentId}") // 경로 변경: 게시글 ID 없이 댓글 ID만으로 접근
    public ResponseEntity<FreeboardCommentResponseDTO> updateComment(
            @Parameter(description = "수정할 댓글의 고유 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer commentId,
            @Valid @org.springframework.web.bind.annotation.RequestBody FreeboardCommentRequestDTO requestDto) {

        FreeboardCommentResponseDTO responseDto = freeboardCommentService.updateComment(commentId, requestDto);
        return ResponseEntity.ok(responseDto); // HTTP 200 OK
    }

    @Operation(summary = "댓글 삭제", description = "특정 ID의 댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "삭제할 댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters({
            @Parameter(name = "commentId", description = "삭제할 댓글의 고유 ID", required = true, example = "1", in = ParameterIn.PATH, schema = @Schema(type = "integer")),
            @Parameter(name = "userId", description = "삭제를 요청하는 사용자의 ID (임시 권한 확인용)", required = true, example = "wansu00", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @DeleteMapping("/comments/{commentId}") // 경로 변경: 게시글 ID 없이 댓글 ID만으로 접근
    public ResponseEntity<Void> deleteComment(
            @PathVariable Integer commentId,
            @RequestParam String userId) { // 임시 권한 확인을 위해 요청 파라미터로 userId 받음

        freeboardCommentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @Operation(summary = "게시글 좋아요 토글", description = "특정 게시글에 대한 사용자의 좋아요 상태를 추가하거나 삭제(토글)합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 처리 성공",
                    content = @Content(schema = @Schema(implementation = PostLikeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 사용자 ID 누락)"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "좋아요를 누르는 사용자의 ID를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = PostLikeRequestDTO.class))
    )
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostLikeResponseDTO> togglePostLike(
            @Parameter(description = "좋아요를 누를 게시글의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer postId,
            @Valid @RequestBody PostLikeRequestDTO requestDto) {

        PostLikeResponseDTO responseDto = freeboardPostService.togglePostLike(postId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "댓글 좋아요 토글", description = "특정 댓글에 대한 사용자의 좋아요 상태를 추가하거나 삭제(토글)합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 처리 성공",
                    content = @Content(schema = @Schema(implementation = CommentLikeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 사용자 ID 누락)"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "좋아요를 누르는 사용자의 ID를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = CommentLikeRequestDTO.class))
    )
    @PostMapping("/comments/{commentId}/like") // 댓글 좋아요 경로
    public ResponseEntity<CommentLikeResponseDTO> toggleCommentLike(
            @Parameter(description = "좋아요를 누를 댓글의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentLikeRequestDTO requestDto) {

        CommentLikeResponseDTO responseDto = freeboardCommentService.toggleCommentLike(commentId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "게시글 신고", description = "특정 게시글을 신고합니다. 한 사용자는 게시글당 한 번만 신고할 수 있으며, 자신의 글은 신고할 수 없습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 신고 성공", // 신고는 리소스를 생성하므로 201 Created 사용 가능
                    content = @Content(schema = @Schema(implementation = ReportSuccessResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 사용자 ID 누락)"),
            @ApiResponse(responseCode = "404", description = "게시글 또는 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 신고한 게시글이거나 자신의 게시글을 신고 시도 (IllegalStateException을 GlobalExceptionHandler에서 409 Conflict로 매핑 고려)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "신고하는 사용자의 ID를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = PostReportRequestDTO.class))
    )
    @PostMapping("/{postId}/report")
    public ResponseEntity<ReportSuccessResponseDTO> reportPost(
            @Parameter(description = "신고할 게시글의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer postId,
            @Valid @RequestBody PostReportRequestDTO requestDto) {

        ReportSuccessResponseDTO responseDto = freeboardPostService.reportPost(postId, requestDto);
        // 신고는 새 리소스를 생성하는 것이므로 201 Created 반환
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri(); // 현재 요청 URI를 Location으로
        return ResponseEntity.created(location).body(responseDto);
    }

    @Operation(summary = "댓글 신고", description = "특정 댓글을 신고합니다. 한 사용자는 댓글당 한 번만 신고할 수 있으며, 자신의 댓글은 신고할 수 없습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 신고 성공",
                    content = @Content(schema = @Schema(implementation = ReportSuccessResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 사용자 ID 누락)"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 신고한 댓글이거나 자신의 댓글을 신고 시도"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "신고하는 사용자의 ID를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = CommentReportRequestDTO.class))
    )
    @PostMapping("/comments/{commentId}/report")
    public ResponseEntity<ReportSuccessResponseDTO> reportComment(
            @Parameter(description = "신고할 댓글의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentReportRequestDTO requestDto) {

        ReportSuccessResponseDTO responseDto = freeboardCommentService.reportComment(commentId, requestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    @Operation(summary = "[관리자] 신고된 게시글 목록 조회", description = "신고된 게시글 목록을 신고 횟수, 작성자 정보 등과 함께 페이징하여 조회합니다. (관리자용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고된 게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (관리자 아님 - 인증 연동 후)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters({
            @Parameter(name = "page", description = "요청할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "size", description = "한 페이지에 보여줄 항목 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            // sort 파라미터 설명은 유지하되, reportCount는 현재 PageableDefault에서 제거
            @Parameter(name = "sort", description = "정렬 조건 (예: postCreatedAt,desc). JPQL에 정의된 기본 정렬 외 다른 정렬은 엔티티 필드 기준.", example = "postCreatedAt,desc", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @GetMapping("/reports/posts")
    // @PreAuthorize("hasRole('ADMIN')") // TODO: 실제 인증 연동 후 관리자 권한 체크 추가
    public ResponseEntity<PageResponseDTO<ReportedPostEntryDTO>> getReportedPosts(
            // @PageableDefault에서 sort = "reportCount" 제거. size와 기본 정렬 방향(만약 필요하다면 다른 필드로)만 남기거나,
            // JPQL의 ORDER BY에 완전히 의존한다면 sort 자체를 빼도 무방합니다.
            // 여기서는 size만 남기고, 정렬은 JPQL의 ORDER BY를 따르도록 합니다.
            @PageableDefault(size = 10) Pageable pageable) {

        PageResponseDTO<ReportedPostEntryDTO> response = freeboardPostService.getReportedPosts(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 신고된 댓글 목록 조회", description = "신고된 댓글 목록을 신고 횟수, 작성자 정보, 원본 게시글 ID 등과 함께 페이징하여 조회합니다. (관리자용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고된 댓글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponseDTO.class))), // 실제로는 PageResponseDTO<ReportedCommentEntryDTO>
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (관리자 아님 - 인증 연동 후)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters({
            @Parameter(name = "page", description = "요청할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "size", description = "한 페이지에 보여줄 항목 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "sort", description = "정렬 조건. JPQL에 정의된 기본 정렬 외 다른 정렬은 엔티티 필드 기준.", example = "commentCreatedAt,asc", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @GetMapping("/reports/comments") // 관리자용 경로 예시
    // @PreAuthorize("hasRole('ADMIN')") // TODO: 실제 인증 연동 후 관리자 권한 체크 추가
    public ResponseEntity<PageResponseDTO<ReportedCommentEntryDTO>> getReportedComments(
            @PageableDefault(size = 10) Pageable pageable) {

        PageResponseDTO<ReportedCommentEntryDTO> response = freeboardCommentService.getReportedComments(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 게시글 숨김/공개 처리", description = "특정 게시글의 숨김 또는 공개 상태를 변경합니다. (관리자용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = FreeboardPostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (isHidden 값 누락 등)"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (관리자 아님 - 인증 연동 후)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "변경할 게시글의 숨김 상태(isHidden)를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = PostVisibilityRequestDTO.class))
    )
    @PatchMapping("/posts/{postId}/visibility") // 관리자용 경로 예시: /admin/posts/{postId}/visibility 등도 고려 가능
    // @PreAuthorize("hasRole('ADMIN')") // TODO: 실제 인증 연동 후 관리자 권한 체크 추가
    public ResponseEntity<FreeboardPostResponseDTO> updatePostVisibility(
            @Parameter(description = "상태를 변경할 게시글의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer postId,
            @Valid @RequestBody PostVisibilityRequestDTO requestDto) {

        FreeboardPostResponseDTO responseDto = freeboardPostService.updatePostVisibility(postId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "[관리자] 댓글 숨김/공개 처리", description = "특정 댓글의 숨김 또는 공개 상태를 변경합니다. (관리자용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = FreeboardCommentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (isHidden 값 누락 등)"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (관리자 아님 - 인증 연동 후)"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "변경할 댓글의 숨김 상태(isHidden)를 담은 DTO",
            required = true,
            content = @Content(schema = @Schema(implementation = CommentVisibilityRequestDTO.class))
    )
    @PatchMapping("/comments/{commentId}/visibility") // 댓글 숨김/공개 API 경로
    // @PreAuthorize("hasRole('ADMIN')") // TODO: 실제 인증 연동 후 관리자 권한 체크 추가
    public ResponseEntity<FreeboardCommentResponseDTO> updateCommentVisibility(
            @Parameter(description = "상태를 변경할 댓글의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer commentId,
            @Valid @RequestBody CommentVisibilityRequestDTO requestDto) {

        FreeboardCommentResponseDTO responseDto = freeboardCommentService.updateCommentVisibility(commentId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "내 자유게시판 활동 목록 조회 (통합)", description = "현재 사용자가 작성한 게시글 및 댓글 전체를 최신순으로 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 활동 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponseDTO.class))), // 실제로는 PageResponseDTO<FreeboardUserActivityItemDTO>
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters({
            // userId는 실제로는 토큰에서 추출하거나 @AuthenticationPrincipal User user로 받아야 함.
            // 현재는 임시로 @RequestParam으로 받습니다.
            @Parameter(name = "userId", description = "활동을 조회할 사용자의 ID (인증 연동 전 임시)", required = true, example = "wansu00", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "page", description = "요청할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "size", description = "한 페이지에 보여줄 항목 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "sort", description = "정렬 조건 (예: createdAt,desc). DTO의 createdAt 필드 기준.", example = "createdAt,desc", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @GetMapping("/activity/my") // "/users/me/activity" 또는 "/member/activity" 등도 가능
    // @PreAuthorize("isAuthenticated()") // TODO: 실제 인증 연동 후 추가
    public ResponseEntity<PageResponseDTO<FreeboardUserActivityItemDTO>> getMyFreeboardActivity(
            @RequestParam String userId, // 실제로는 인증된 사용자 정보를 사용해야 함
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        // pageable의 sort가 "createdAt"으로 오면, 서비스 로직 내 메모리 정렬에서 이를 활용합니다.
        // DB 조회 시에는 각 엔티티의 생성일시 필드로 정렬해서 가져오는 것이 좋습니다.

        PageResponseDTO<FreeboardUserActivityItemDTO> response = freeboardPostService.getUserFreeboardActivity(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 쓴 댓글 목록 조회", description = "특정 사용자가 작성한 댓글 목록을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponseDTO.class))), // 실제로는 PageResponseDTO<FreeboardCommentResponseDTO>
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters({
            @Parameter(name = "userId", description = "댓글을 조회할 작성자의 User ID", required = true, example = "wansu00", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
            @Parameter(name = "page", description = "요청할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "size", description = "한 페이지에 보여줄 댓글 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "sort", description = "정렬 조건 (예: commentCreatedAt,desc). 기본값: commentCreatedAt,desc", example = "commentCreatedAt,desc", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @GetMapping("/comments/by-user") // API 경로 예시
    // @PreAuthorize("isAuthenticated()") // TODO: 실제 인증 연동 후, 자신의 댓글만 보게 하거나, 관리자는 타인 ID 조회 가능하게 할 수 있음
    public ResponseEntity<PageResponseDTO<FreeboardCommentResponseDTO>> getCommentsByAuthor(
            @RequestParam String userId, // "내 활동"이므로 실제로는 인증된 사용자 ID 사용
            @PageableDefault(size = 10, sort = "commentCreatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponseDTO<FreeboardCommentResponseDTO> response = freeboardCommentService.getCommentsByAuthor(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 전체 신고된 활동 목록 조회 (통합)", description = "신고된 모든 게시글 및 댓글 활동을 최신순으로 페이징하여 조회합니다. (관리자용)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고된 활동 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters({
            @Parameter(name = "page", description = "요청할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "size", description = "한 페이지에 보여줄 항목 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "sort", description = "정렬 조건 (예: reportCreatedAt,desc). DTO의 reportCreatedAt 필드 기준.", example = "reportCreatedAt,desc", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @GetMapping("/admin/reports/all") // 관리자용 경로 예시
    // @PreAuthorize("hasRole('ADMIN')") // TODO: 실제 인증 연동 후 관리자 권한 체크 추가
    public ResponseEntity<PageResponseDTO<AdminReportedActivityItemDTO>> getAllReportedActivities(
            @PageableDefault(size = 10, sort = "reportCreatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponseDTO<AdminReportedActivityItemDTO> response = adminReportViewService.getAllReportedActivities(pageable);
        return ResponseEntity.ok(response);
    }


}