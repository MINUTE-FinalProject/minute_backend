package com.minute.example.controller;

import com.minute.example.dto.ExampleRequestDto; // 방금 만드신 DTO 임포트
import com.minute.example.dto.ExampleResponseDto; // 방금 만드신 DTO 임포트
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Spring Web 어노테이션 임포트

import java.net.URI; // ResponseEntity.created() 사용 시 필요

// @Tag: API들을 그룹화합니다. Swagger UI에서 이 이름으로 섹션이 생깁니다.
@Tag(name = "00. 예시 API (팀 가이드용)", description = "Swagger 어노테이션 사용법을 안내하고 API 구조를 보여주기 위한 예시입니다.")
@RestController // 이 클래스가 RESTful API 컨트롤러임을 나타냅니다.
@RequestMapping("/api/v1/example") // 이 컨트롤러의 모든 API는 /api/v1/example 로 시작합니다.
public class ExampleController {

    // @Operation: 특정 API 엔드포인트에 대한 설명을 제공합니다.
    @Operation(summary = "예시 GET 요청 (데이터 조회)",
            description = "ID를 사용하여 특정 예시 데이터를 조회합니다. Query Parameter, Path Variable, Header 사용법을 보여줍니다.")
    // @ApiResponses: 이 API가 반환할 수 있는 다양한 HTTP 응답 상태와 그 설명을 정의합니다.
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = ExampleResponseDto.class))), // 성공 시 응답 DTO 명시
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식 (예: 파라미터 타입 불일치)"),
            @ApiResponse(responseCode = "404", description = "요청한 ID에 해당하는 데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 처리 오류")
    })
    // @Parameters: 여러 파라미터를 한 번에 정의할 때 사용합니다. 각 파라미터는 @Parameter로 정의합니다.
    @Parameters({
            @Parameter(name = "id", description = "조회할 데이터의 고유 ID (경로 변수)", required = true, example = "123", in = ParameterIn.PATH),
            @Parameter(name = "type", description = "데이터의 유형 (쿼리 파라미터)", required = false, example = "A", in = ParameterIn.QUERY),
            @Parameter(name = "X-Custom-Header", description = "사용자 정의 헤더 값 예시", required = false, example = "CustomValue123", in = ParameterIn.HEADER)
    })
    @GetMapping("/{id}") // HTTP GET 요청을 /api/v1/example/{id} 경로로 매핑합니다.
    public ResponseEntity<ExampleResponseDto> getExampleData(
            @PathVariable Long id, // 경로 변수 {id}를 받습니다. @Parameter의 name과 일치해야 합니다.
            @RequestParam(required = false, defaultValue = "DefaultType") String type, // 쿼리 파라미터 'type'을 받습니다.
            @RequestHeader(name = "X-Custom-Header", required = false) String customHeader) { // HTTP 헤더 'X-Custom-Header' 값을 받습니다.



        // --- 실제 비즈니스 로직은 여기에 작성합니다 ---
        // 이 예시에서는 로직을 최소화하고, 어노테이션 사용에 집중합니다.
        // 팀원들에게는 "이 부분에 실제 데이터 조회/처리 로직이 들어갑니다." 라고 안내해주세요.
        // 예시 응답 생성:
        if (id > 9000) { // 간단한 예외 케이스 처리
            // 이 경우, @ApiResponse(responseCode = "404", ...)에 해당하는 상황을 연출할 수 있습니다.
            // 실제로는 서비스 계층에서 예외를 던지고 @ControllerAdvice 등으로 처리하는 것이 일반적입니다.
            return ResponseEntity.notFound().build();
        }
        String message = String.format("GET 요청 처리 완료: id=%d, type=%s, customHeader=%s", id, type, customHeader);
        ExampleResponseDto responseDto = new ExampleResponseDto(id, message, "조회된 항목 이름 (예시)");
        // --- 여기까지 비즈니스 로직 ---

        return ResponseEntity.ok(responseDto); // HTTP 200 OK 응답과 함께 responseDto를 반환합니다.
    }

    @Operation(summary = "예시 POST 요청 (데이터 생성)",
            description = "새로운 예시 데이터를 시스템에 생성합니다. Request Body 사용법을 보여줍니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "데이터 생성 성공",
                    content = @Content(schema = @Schema(implementation = ExampleResponseDto.class))), // 성공 시 응답 DTO
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (예: 필수 필드 누락, 데이터 유효성 검사 실패)")
            // 필요한 다른 응답 코드들도 추가할 수 있습니다.
    })
    @PostMapping // HTTP POST 요청을 /api/v1/example 경로로 매핑합니다.
    public ResponseEntity<ExampleResponseDto> createExampleData(
            // @io.swagger.v3.oas.annotations.parameters.RequestBody: Swagger에서 요청 본문을 설명하는 어노테이션입니다.
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "생성할 데이터의 요청 DTO입니다. 'itemName'은 필수 항목입니다.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExampleRequestDto.class)) // 요청 본문의 DTO 명시
            )
            @org.springframework.web.bind.annotation.RequestBody ExampleRequestDto requestDto) { // Spring의 @RequestBody: HTTP 요청 본문을 DTO로 변환

        // --- 실제 비즈니스 로직은 여기에 작성합니다 ---
        // 예시:
        // if (requestDto.getItemName() == null || requestDto.getItemName().trim().isEmpty()) {
        //     // 이 경우, @ApiResponse(responseCode = "400", ...)에 해당하는 상황을 연출할 수 있습니다.
        //     // 실제로는 @Valid 어노테이션과 함께 DTO 유효성 검사를 사용하고, 예외 처리를 합니다.
        //     return ResponseEntity.badRequest().body(new ExampleResponseDto(null, "itemName은 필수입니다.", null));
        // }
        long newResourceId = System.currentTimeMillis() % 1000; // 임시로 새 리소스 ID 생성
        String message = String.format("POST 요청 처리 완료: 생성된 항목 이름 - %s", requestDto.getItemName());
        ExampleResponseDto responseDto = new ExampleResponseDto(newResourceId, message, requestDto.getItemName());
        // --- 여기까지 비즈니스 로직 ---

        // HTTP 201 Created 응답을 반환합니다.
        // 생성된 리소스의 URI를 Location 헤더에 포함하는 것이 RESTful API의 좋은 관례입니다.
        URI location = URI.create(String.format("/api/v1/example/%d", newResourceId));
        return ResponseEntity.created(location).body(responseDto);
    }
}