package com.minute.video.controller;

import com.minute.video.dto.VideoLikesRequestDTO;
import com.minute.video.dto.VideoLikesResponseDTO;
import com.minute.video.service.VideoLikesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Like", description = "좋아요 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class VideoLikesController {

    private final VideoLikesService videoLikesService;

    @Operation(summary = "영상 좋아요",description = "사용자가 해당 영상을 좋아요 상태로 저장합니다. ")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요가 정상적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. userId나 videoId를 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @PostMapping("/api/v1/videos/{videoId}/like")
    public ResponseEntity<Void> like(@PathVariable String videoId, @RequestParam String userId){
        VideoLikesRequestDTO dto = VideoLikesRequestDTO.builder()
                .userId(userId)
                .videoId(videoId)
                .build();
        videoLikesService.savelike(dto);

        // 204 No Content를 반환해서 요청은 성공했지만 응답 바디는 없다고 명시한다.
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 좋아요 영상 조회", description = "해당 사용자가 좋아요 한 영상 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요한 영상 목록을 정상적으로 조회하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 사용자 ID입니다. 다시 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @GetMapping("/api/v1/auth/{userId}/likes")
    public List<VideoLikesResponseDTO> list(@PathVariable String userId) {
        return videoLikesService.getUserLikedVideos(userId);
    }
}
