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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
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
    @PostMapping("/{userId}/videos/{videoId}/like")
    public ResponseEntity<Void> like(
            @PathVariable String userId,
            @PathVariable String videoId){
        if (videoId == null || videoId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "videoId is missing");
        }
        videoLikesService.saveLike(userId, videoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "영상 좋아요 삭제",description = "사용자가 해당 영상에 대한 좋아요를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요 정상적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. userId나 videoId를 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @DeleteMapping("/{userId}/videos/{videoId}/like")
    public ResponseEntity<Void> delete(
            @PathVariable String userId,
            @PathVariable String videoId) {

        videoLikesService.deleteLike(userId, videoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 좋아요 영상 조회", description = "해당 사용자가 좋아요 한 영상 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요한 영상 목록을 정상적으로 조회하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 사용자 ID입니다. 다시 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @GetMapping("/{userId}/likes")
    public List<VideoLikesResponseDTO> list(
            @PathVariable String userId) {
        return videoLikesService.getUserLikedVideos(userId);
    }
}
