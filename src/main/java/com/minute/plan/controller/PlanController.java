package com.minute.plan.controller;

import com.minute.plan.dto.request.PlanRequestDTO;
import com.minute.plan.dto.response.PlanResponseDTO;
import com.minute.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Plan", description = "Plan 관련 API")
@RestController
@RequestMapping("/api/v1/plans")
@Validated
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;

    @Operation(description = "특정 날짜 일정 조회")
    @GetMapping
    public List<PlanResponseDTO> getByDate(Principal principal,
                                           @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                           LocalDate date) {
        String userId = principal.getName();
        return planService.getPlansByUserAndDate(userId, date);
    }

    @Operation(summary = "일정 작성")
    @PostMapping
    public PlanResponseDTO create(
            @RequestBody @Valid PlanRequestDTO dto,
            Principal principal) {
        String userId = principal.getName();

        return planService.create(userId, dto);
    }

    @Operation(summary = "일정 수정")
    @PutMapping("/{planId}")
    public PlanResponseDTO update(
            @PathVariable Integer planId,
            @RequestBody @Valid PlanRequestDTO dto,
            Principal principal
    ) {
        return planService.update(principal.getName(), planId, dto);
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{planId}")
    public void delete(
            @PathVariable Integer planId,
            Principal principal
    ) {
        String userId = principal.getName();
        planService.delete(userId, planId);
    }
}
