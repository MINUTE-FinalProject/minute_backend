package com.minute.video.controller;

import com.minute.video.dto.WatchHistoryRequestDTO;
import com.minute.video.dto.WatchHistoryResponseDTO;
import com.minute.video.service.WatchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "WatchHistory",description = "영상 시청 기록 API")
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    @Operation(summary = "시청 기록 저장",description = "사용자의 시청 기록을 저장합니다.")
    @PostMapping("/api/watch-history")
    public void save(@RequestBody WatchHistoryRequestDTO dto) {
        watchHistoryService.saveWatchHistory(dto);
    }

    @Operation(summary = "사용자 시청 기록 조회",description = "해당 사용자의 시청 기록을 최신순으로 반환합니다.")
    @GetMapping("/api/users/{userId}/watch-history")
    public List<WatchHistoryResponseDTO> list(@PathVariable String userId){
        return watchHistoryService.getUserWatchHistory(userId);
    }
}
