package com.minute.calendar.controller;

import com.minute.calendar.dto.response.CalendarResponseDTO;
import com.minute.plan.dto.response.PlanResponseDTO;
import com.minute.plan.service.PlanService;
import com.minute.checklist.dto.response.ChecklistResponseDTO;
import com.minute.checklist.service.ChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Calendar", description = "캘린더 통합 조회 API")
@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final PlanService planService;
    private final ChecklistService checklistService;

    @Operation(summary = "특정 날짜의 Plan + Checklist 동시에 조회")
    @GetMapping("/details")
    public CalendarResponseDTO getDetails(
            Principal principal,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        String userId = principal.getName();

        List<PlanResponseDTO> plans = planService.getPlansByUserAndDate(userId, date);
        List<ChecklistResponseDTO> checklists = checklistService.getChecklistsForCalendar(userId, date);

        return new CalendarResponseDTO(plans, checklists);
    }
}
