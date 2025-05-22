package com.minute.board.notice.controller;

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.notice.dto.request.NoticeCreateRequestDTO;
import com.minute.board.notice.dto.request.NoticeUpdateRequestDTO;
import com.minute.board.notice.dto.response.NoticeDetailResponseDTO;
import com.minute.board.notice.dto.response.NoticeListResponseDTO;
import com.minute.board.notice.service.NoticeService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "01. 공지사항 API", description = "공지사항 관련 API 목록입니다.") // API 그룹화
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 전체 목록 조회", // API 요약 설명
            description = "페이징 처리된 공지사항 전체 목록을 조회합니다. 중요 공지 상단 정렬 및 최신순으로 기본 정렬됩니다.") // API 상세 설명
    @ApiResponses(value = { // API 응답 케이스 정의
            @ApiResponse(responseCode = "200", description = "공지사항 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponseDTO.class))), // 성공 응답 DTO 명시
            // 여기에 PageResponseDTO<NoticeListResponseDTO>를 명시적으로 표현하려면 springdoc-openapi의 도움을 받거나,
            // 응답 스키마를 더 상세히 기술해야 할 수 있습니다.
            // 우선은 PageResponseDTO.class로 두고, 실제 Swagger UI에서 어떻게 보이는지 확인 후 조정합니다.
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터 (예: 페이지 번호 음수)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    // Pageable 파라미터에 대한 설명 (springdoc-openapi 사용 시 자동 생성되는 경우가 많음)
    // 명시적으로 추가하여 더 자세한 정보를 제공할 수 있습니다.
    @Parameters({
            @Parameter(name = "page", description = "요청할 페이지 번호 (0부터 시작)", in = ParameterIn.QUERY,
                    schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "페이지 당 공지사항 수", in = ParameterIn.QUERY,
                    schema = @Schema(type = "integer", defaultValue = "10")),
            @Parameter(name = "sort", description = "정렬 기준 (예: 'noticeIsImportant,desc' 또는 'noticeCreatedAt,asc'). " +
                    "여러 정렬 기준을 함께 사용할 수 있습니다 (예: 'noticeIsImportant,desc&sort=noticeCreatedAt,desc'). " +
                    "기본값: 중요도 내림차순, 작성일 내림차순.",
                    in = ParameterIn.QUERY, allowEmptyValue = true,
                    schema = @Schema(type = "array", implementation = String.class, example = "[\"noticeIsImportant,desc\", \"noticeCreatedAt,desc\"]"))
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<NoticeListResponseDTO>> getNoticeList(
            @PageableDefault(
                    size = 10,
                    sort = {"noticeIsImportant", "noticeCreatedAt"},
                    direction = Sort.Direction.DESC
            ) Pageable pageable) { // @Parameter 어노테이션을 Pageable 파라미터 자체에 달 수도 있습니다.

        PageResponseDTO<NoticeListResponseDTO> response = noticeService.getNoticeList(pageable);
        return ResponseEntity.ok(response);
    }

    // GET /api/notices/{id}
    @Operation(summary = "공지사항 상세 조회",
            description = "특정 ID의 공지사항 상세 정보를 조회합니다. 조회 시 해당 공지사항의 조회수가 1 증가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = NoticeDetailResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 ID에 해당하는 공지사항을 찾을 수 없음 (GlobalExceptionHandler 처리)",
                    content = @Content(schema = @Schema(example = "{\"status\":\"error\",\"message\":\"해당 ID의 공지사항을 찾을 수 없습니다: 999\"}"))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{noticeId}") // 경로 변수로 noticeId를 받습니다.
    public ResponseEntity<NoticeDetailResponseDTO> getNoticeDetail(
            @Parameter(name = "noticeId", description = "조회할 공지사항의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer noticeId) { // @PathVariable을 사용하여 경로 변수 값을 가져옵니다.

        NoticeDetailResponseDTO response = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok(response); // 200 OK 상태와 함께 응답 본문 반환
    }

    // POST /api/notices
    @Operation(summary = "새 공지사항 작성 (관리자 권한 필요)",
            description = "새로운 공지사항을 등록합니다. 이 API는 'ADMIN' 역할을 가진 사용자만 호출할 수 있습니다.",
            security = @SecurityRequirement(name = "bearerAuth")) // 스웨거에서 JWT 인증 필요 명시
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "공지사항 생성 성공",
                    content = @Content(schema = @Schema(implementation = NoticeDetailResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 누락 또는 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (ADMIN 역할이 아님)"),
            @ApiResponse(responseCode = "404", description = "작성자 정보를 찾을 수 없음"), // 서비스에서 발생 가능
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping
    public ResponseEntity<NoticeDetailResponseDTO> createNotice(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "생성할 공지사항의 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NoticeCreateRequestDTO.class))
            )
            @RequestBody NoticeCreateRequestDTO requestDto, // Spring의 @RequestBody 어노테이션
            Authentication authentication) { // Spring Security의 Authentication 객체를 통해 현재 사용자 정보 접근

        // 현재 인증된 사용자의 ID (principal의 name을 userId로 사용한다고 가정)
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // String authenticatedUserId = userDetails.getUsername();
        // 또는 만약 Principal 객체가 User 엔티티의 ID를 직접 반환하도록 커스터마이징했다면,
        String authenticatedUserId = authentication.getName(); // 일반적으로 username(ID)을 반환

        // 서비스 호출하여 공지사항 생성
        NoticeDetailResponseDTO createdNotice = noticeService.createNotice(requestDto, authenticatedUserId);

        // 생성된 리소스의 URI를 Location 헤더에 포함하여 201 Created 응답 반환
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdNotice.getNoticeId())
                .toUri();

        return ResponseEntity.created(location).body(createdNotice);
    }

    // PUT /api/notices/{noticeId}
    @Operation(summary = "공지사항 수정 (관리자 권한 필요)",
            description = "기존 공지사항의 내용을 수정합니다. 이 API는 'ADMIN' 역할을 가진 사용자만 호출할 수 있습니다. DTO의 필드는 선택 사항이며, 제공된 필드만 업데이트됩니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항 수정 성공",
                    content = @Content(schema = @Schema(implementation = NoticeDetailResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 누락 또는 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (ADMIN 역할이 아님 또는 해당 공지 수정 권한 없음)"),
            @ApiResponse(responseCode = "404", description = "수정할 공지사항 또는 작성자 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponseDTO> updateNotice(
            @Parameter(name = "noticeId", description = "수정할 공지사항의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer noticeId,
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 공지사항의 정보. 모든 필드는 선택적입니다.",
                    required = true, // 요청 본문 자체는 필수
                    content = @Content(schema = @Schema(implementation = NoticeUpdateRequestDTO.class))
            )
            @RequestBody NoticeUpdateRequestDTO requestDto,
            Authentication authentication) {

        String authenticatedUserId = authentication.getName(); // 현재 인증된 사용자의 ID
        NoticeDetailResponseDTO updatedNotice = noticeService.updateNotice(noticeId, requestDto, authenticatedUserId);
        return ResponseEntity.ok(updatedNotice); // 200 OK와 함께 수정된 공지사항 정보 반환
    }

    // DELETE /api/notices/{noticeId}
    @Operation(summary = "공지사항 삭제 (관리자 권한 필요)",
            description = "특정 ID의 공지사항을 삭제합니다. 이 API는 'ADMIN' 역할을 가진 사용자만 호출할 수 있습니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "공지사항 삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 누락 또는 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음 (ADMIN 역할이 아님 또는 해당 공지 삭제 권한 없음)"),
            @ApiResponse(responseCode = "404", description = "삭제할 공지사항을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 성공 시 HTTP 204 No Content 응답을 명시적으로 반환
    public ResponseEntity<Void> deleteNotice(
            @Parameter(name = "noticeId", description = "삭제할 공지사항의 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer noticeId,
            Authentication authentication) {

        String authenticatedUserId = authentication.getName(); // 현재 인증된 사용자의 ID
        noticeService.deleteNotice(noticeId, authenticatedUserId);

        // 성공적으로 삭제되면 내용 없이 204 No Content 응답을 보내는 것이 일반적입니다.
        // @ResponseStatus(HttpStatus.NO_CONTENT)를 사용하거나,
        // return ResponseEntity.noContent().build(); 를 사용할 수 있습니다.
        // 여기서는 @ResponseStatus를 사용하고 ResponseEntity<Void>를 반환하도록 했습니다.
        // 만약 간단한 성공 메시지를 JSON으로 보내고 싶다면 ResponseEntity.ok().body(Map.of("message", "삭제되었습니다")); 와 같이 할 수도 있습니다.
        return ResponseEntity.noContent().build();
    }
}