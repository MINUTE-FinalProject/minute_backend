package com.minute.mypage.controller;

import com.minute.mypage.dto.response.DotResponseDTO;
import com.minute.mypage.service.MyPageService;
import com.minute.plan.dto.response.PlanResponseDTO;
import com.minute.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    private final PlanService planService;

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

    @Operation(summary = "마이페이지용 특정 날짜 일정 조회")
    @GetMapping("/plans")
    public List<PlanResponseDTO> getMyPagePlans(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        String userId = principal.getName();
        return myPageService.getPlansOnly(userId, date);
    }
}
