package com.minute.video.controller;

import com.minute.video.dto.VideoDislikesResponseDTO;
import com.minute.video.service.VideoDislikeService;
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
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "DisLike", description = "싫어요 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class VideoDislikesController {

    private final VideoDislikeService videoDislikeService;

    @Operation(summary = "영상 싫어요 등록/취소", description = "사용자가 해당 영상에 대해 싫어요를 토글합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요가 정상적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. userId나 videoId를 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @PostMapping("/{userId}/videos/{videoId}/dislike")
    public ResponseEntity<Void> toggleDislike(
            @PathVariable String userId,
            @PathVariable String videoId) {
        videoDislikeService.toggleDislike(userId, videoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 싫어요 영상 목록 조회", description = "해당 사용자가 싫어요 누른 영상 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요한 영상 목록을 정상적으로 조회하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 사용자 ID입니다. 다시 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @GetMapping("/{userId}/dislikes")
    public ResponseEntity<List<VideoDislikesResponseDTO>> getUserDislikes(
            @PathVariable String userId) {
        return ResponseEntity.ok(videoDislikeService.getUserDislikedVideos(userId));
    }
}