package com.minute.video.controller;

import com.minute.video.dto.WatchHistoryRequestDTO;
import com.minute.video.dto.WatchHistoryResponseDTO;
import com.minute.video.service.WatchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "WatchHistory",description = "영상 시청 기록 API")
@SecurityRequirement(name = "bearerAuth")
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    @Operation(summary = "시청 기록 저장",description = "사용자의 시청 기록을 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시청 기록이 정상적으로 저장되었습니다."),
            @ApiResponse(responseCode = "400", description = "요청하신 시청 기록 정보가 올바르지 않습니다. 다시 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @PostMapping("/api/v1/watch-history")
    public void save(@RequestBody WatchHistoryRequestDTO dto) {
        watchHistoryService.saveWatchHistory(dto);
    }

    @Operation(summary = "사용자 시청 기록 조회",description = "해당 사용자의 시청 기록을 최신순으로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시청 기록 목록을 정상적으로 반환하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 사용자 ID입니다. 다시 확인해 주세요."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
    })
    @GetMapping("/api/v1/auth/{userId}/watch-history")
    public List<WatchHistoryResponseDTO> list(@PathVariable String userId){
        return watchHistoryService.getUserWatchHistory(userId);
    }
}
