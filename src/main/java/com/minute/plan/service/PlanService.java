// src/main/java/com/minute/plan/service/PlanService.java
package com.minute.plan.service;

import com.minute.plan.dto.response.PlanResponseDTO;
import com.minute.plan.entity.Plan;
import com.minute.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;

    /** 한 달치 dot 조회용 */
    public List<LocalDate> getPlanDatesInMonth(String userId, YearMonth ym) {
        var start = ym.atDay(1);
        var end   = ym.atEndOfMonth();
        return planRepository.findTravelDatesInMonth(userId, start, end);
    }

    /** 특정 날짜의 Plan → DTO 매핑 */
    public List<PlanResponseDTO> getPlansByUserAndDate(String userId, LocalDate date) {
        return planRepository.findAllByUser_UserIdAndTravelDate(userId, date)
                .stream()
                .map(plan -> new PlanResponseDTO(
                        plan.getPlanId(),
                        plan.getTitle(),
                        plan.getDescription(),
                        plan.getStartTime(),
                        plan.getEndTime()
                ))
                .collect(Collectors.toList());
    }
}
