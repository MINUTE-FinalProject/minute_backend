// src/main/java/com/minute/checklist/service/ChecklistService.java
package com.minute.checklist.service;

import com.minute.checklist.dto.response.ChecklistResponseDTO;
import com.minute.checklist.entity.Checklist;
import com.minute.checklist.repository.ChecklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChecklistService {
    private final ChecklistRepository checklistRepository;

    /** 한 달치 dot 조회용 */
    public List<LocalDate> getChecklistDatesInMonth(String userId, YearMonth ym) {
        var start = ym.atDay(1);
        var end   = ym.atEndOfMonth();
        return checklistRepository.findTravelDatesInMonth(userId, start, end);
    }

    /** 특정 날짜의 Checklist → DTO 매핑 */
    public List<ChecklistResponseDTO> getChecklistsByUserAndDate(String userId, LocalDate date) {
        return checklistRepository.findAllByUser_UserIdAndTravelDate(userId, date)
                .stream()
                .map(item -> {
                    Integer planId = null;
                    if (item.getPlan() != null) {
                        planId = item.getPlan().getPlanId();
                    }
                    return new ChecklistResponseDTO(
                            item.getChecklistId(),
                            planId,
                            item.getItemContent(),
                            item.getIsChecked()
                    );
                })
                .collect(Collectors.toList());
    }
}
