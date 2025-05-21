package com.minute.board.notice.controller;

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.notice.dto.NoticeListResponseDTO;
import com.minute.board.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}