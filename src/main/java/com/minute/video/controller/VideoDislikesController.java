package com.minute.video.controller;

import com.minute.video.service.VideoDisLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "DisLike", description = "싫어요 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class VideoDislikesController {

    private final VideoDisLikeService videoDislikeService;

    @Operation(summary = "영상 싫어요", description = "사용자가 해당 영상을 싫어요 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요가 정상적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. userId나 videoId를 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @PostMapping("/{userId}/videos/{videoId}/dislike")
    public ResponseEntity<Void> dislike(
            @PathVariable String userId,
            @PathVariable String videoId) {
        videoDislikeService.toggleDislike(userId, videoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "싫어요 삭제", description = "사용자가 해당 영상의 싫어요를 취소합니다.")
    @DeleteMapping("/{userId}/videos/{videoId}/dislike")
    public ResponseEntity<Void> cancelDislike(
            @PathVariable String userId,
            @PathVariable String videoId) {
        videoDislikeService.toggleDislike(userId, videoId); // 같은 메서드로 toggle
        return ResponseEntity.noContent().build();
    }
}