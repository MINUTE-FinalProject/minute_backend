package com.minute.mypage.controller;

import com.minute.mypage.dto.response.DateDetailResponseDTO;
import com.minute.mypage.dto.response.DotResponseDTO;
import com.minute.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Tag(name = "MyPage", description = "마이페이지 관련 API")
@RestController
@RequestMapping("/api/v1/mypage")
@Validated
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "한 달치 일정·체크리스트 날짜 조회")
    @GetMapping("/dots")
    public List<DotResponseDTO> getDots(
            Principal principal,
            @RequestParam("yearMonth") String yearMonth
    ) {
        String userId = principal.getName();
        YearMonth ym  = YearMonth.parse(yearMonth);
        return myPageService.getMonthlyDots(userId, ym);
    }

    @Operation(summary = "특정 날짜의 일정·체크리스트 조회")
    @GetMapping("/details")
    public DateDetailResponseDTO getDetails(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회할 여행 날짜", example = "2025-05-23")
            LocalDate travelDate
    ) {
        String userId = principal.getName();
        return myPageService.getDateDetails(userId, travelDate);
    }
}
