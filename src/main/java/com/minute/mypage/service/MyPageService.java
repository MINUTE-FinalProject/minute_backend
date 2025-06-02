package com.minute.mypage.service;

import com.minute.checklist.dto.response.ChecklistResponseDTO;
import com.minute.checklist.service.ChecklistService;
import com.minute.mypage.dto.response.DateDetailResponseDTO;
import com.minute.mypage.dto.response.DotResponseDTO;
import com.minute.plan.dto.response.PlanResponseDTO;
import com.minute.plan.service.PlanService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class MyPageService {

    private final PlanService planService;
    private final ChecklistService checklistService;

    public List<DotResponseDTO> getMonthlyDots(String userId, YearMonth ym) {
        List<LocalDate> planDates = planService.getPlanDatesInMonth(userId, ym);
        List<LocalDate> checklistDates = checklistService.getChecklistDatesInMonth(userId, ym);

        // dots API
        Map<LocalDate, String> map = new HashMap<>();
        planDates.forEach(d -> map.put(d, "plan"));
        checklistDates.forEach(d ->
                map.merge(d, "checklist",
                        (oldVal, newVal) -> "plan".equals(oldVal) ? "both" : oldVal));

        return map.entrySet().stream()
                .map(e -> new DotResponseDTO(e.getKey().toString(), e.getValue()))
                .toList();

    }

    // details API
    public DateDetailResponseDTO getDateDetails(String userId, LocalDate date) {
        List<PlanResponseDTO> plans = planService.getPlansByUserAndDate(userId, date);
        List<ChecklistResponseDTO> checklists = checklistService.getChecklistsByUserAndDate(userId, date);
        return new DateDetailResponseDTO(plans, checklists);
    }


}
