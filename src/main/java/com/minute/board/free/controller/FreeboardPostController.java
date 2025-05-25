package com.minute.board.free.controller; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.free.dto.request.FreeboardPostRequestDTO;
import com.minute.board.free.dto.response.FreeboardPostResponseDTO;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
}