package com.minute.mypage.controller;

import com.minute.mypage.dto.response.ResponseDotDTO;
import com.minute.mypage.service.DotService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/mypage")
@SecurityRequirement(name = "bearerAuth")
public class DotController {

    private final DotService dotService;

    @GetMapping("/dots")
    public List<ResponseDotDTO> getDots(@Parameter(hidden = true)
                                            java.security.Principal principal,
                                        @Parameter(description = "조회할 연-월 (yyyy-MM)", required = true, example = "2025-05") @RequestParam String yearMonth) {

        String userId = principal.getName();
        YearMonth ym = YearMonth.parse(yearMonth);

        return dotService.getMonthlyDots(userId, ym);
    }
}
