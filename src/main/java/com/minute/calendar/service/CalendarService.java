package com.minute.calendar.service;

import com.minute.calendar.dto.response.CalendarDetailResponseDTO;
import com.minute.checklist.dto.response.ChecklistResponseDTO;
import com.minute.checklist.service.ChecklistService;
import com.minute.plan.dto.response.PlanResponseDTO;
import com.minute.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final PlanService planService;
    private final ChecklistService checklistService;

    public CalendarDetailResponseDTO getDateDetails(String userId, LocalDate date) {
        List<PlanResponseDTO> plans = planService.getByDate(userId, date);
        List<ChecklistResponseDTO> checklists = checklistService.getByDate(userId, date);

        return CalendarDetailResponseDTO.builder()
                .plans(plans)
                .checklists(checklists)
                .build();

    }
}
