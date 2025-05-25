package com.minute.board.free.controller; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.free.dto.response.FreeboardPostSimpleResponseDTO;
import com.minute.board.free.service.FreeboardPostService;
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

@Tag(name = "01. 자유게시판 API", description = "자유게시판 게시글 관련 API입니다.") // API 그룹 태그
@RestController
@RequestMapping("/api/v1/board/free") // 공통 경로
@RequiredArgsConstructor
public class FreeboardPostController {

    private final FreeboardPostService freeboardPostService;

    @Operation(summary = "자유게시판 게시글 목록 조회", description = "페이징 처리된 자유게시판 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponseDTO.class))) // 실제로는 PageResponseDTO<FreeboardPostSimpleResponseDTO> 지만, Swagger 표현 방식 확인 필요
            // 필요한 경우 다른 응답 코드들 (예: 400, 500) 추가
    })
    @Parameters({
            @Parameter(name = "page", description = "요청할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "size", description = "한 페이지에 보여줄 게시글 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
            @Parameter(name = "sort", description = "정렬 조건 (예: postId,desc 또는 postCreatedAt,asc)", example = "postId,desc", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    @GetMapping
    public ResponseEntity<PageResponseDTO<FreeboardPostSimpleResponseDTO>> getAllPosts(
            // @PageableDefault: 기본 페이징 값 설정 (예: 한 페이지에 10개, postId 기준 내림차순 정렬)
            @PageableDefault(size = 10, sort = "postId", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponseDTO<FreeboardPostSimpleResponseDTO> response = freeboardPostService.getAllPosts(pageable);
        return ResponseEntity.ok(response);
    }

    // 여기에 게시글 상세 조회, 작성, 수정, 삭제 등 다른 API 엔드포인트들이 추가될 예정입니다.
}