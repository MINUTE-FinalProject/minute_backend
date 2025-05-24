package com.minute.mypage.service;

import com.minute.checklist.repository.ChecklistRepository;
import com.minute.mypage.dto.response.ResponseDotDTO;
import com.minute.plan.repository.PlanRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class DotService {

    private final PlanRepository planRepository;
    private final ChecklistRepository checklistRepository;

    public List<ResponseDotDTO> getMonthlyDots(String userId, YearMonth ym) {
        // 한 달 시작, 끝 계산
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // repository에서 조회
        List<LocalDate> planDates = planRepository.findTravelDatesInMonth(userId, startDate, endDate);
        List<LocalDate> checklistDates = checklistRepository.findTravelDatesInMonth(userId, startDate, endDate);

        // 날짜별 타입 결정
        Map<LocalDate, String> map = new HashMap<>();
        planDates.forEach(d -> map.put(d, "plan"));
        checklistDates.forEach(d ->
                map.merge(d, "checklist",
                        (oldVad, newVal) -> "plan".equals(oldVad) ? "both" : oldVad));

        return map.entrySet().stream()
                .map(e -> new ResponseDotDTO(e.getKey().toString(), e.getValue()))
                .toList();

    }


}
