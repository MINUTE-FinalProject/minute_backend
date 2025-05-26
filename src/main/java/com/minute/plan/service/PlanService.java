// src/main/java/com/minute/plan/service/PlanService.java
package com.minute.plan.service;

import com.minute.plan.dto.request.PlanRequestDTO;
import com.minute.plan.dto.response.PlanResponseDTO;
import com.minute.plan.entity.Plan;
import com.minute.plan.repository.PlanRepository;
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final UserRepository userRepository;
    private final PlanRepository planRepository;

    // 한 달치 dot 조회용
    public List<LocalDate> getPlanDatesInMonth(String userId, YearMonth ym) {
        var start = ym.atDay(1);
        var end   = ym.atEndOfMonth();
        return planRepository.findTravelDatesInMonth(userId, start, end);
    }

    // 특정 날짜의 Plan → DTO 매핑 (조회)
    public List<PlanResponseDTO> getPlansByUserAndDate(String userId, LocalDate date) {
        return planRepository.findAllByUser_UserIdAndTravelDate(userId, date)
                .stream()
                .map(plan -> new PlanResponseDTO(
                        plan.getPlanId(),
                        plan.getTravelDate(),
                        plan.getTitle(),
                        plan.getDescription(),
                        plan.getStartTime(),
                        plan.getEndTime()
                ))
                .collect(Collectors.toList());
    }

    // Plan 생성
    @Transactional
    public PlanResponseDTO create(String userId, PlanRequestDTO planRequestDTO) {
        User user = userRepository.findById(userId).orElse(null);

        Plan p = Plan.builder()
                .user(user)
                .travelDate(planRequestDTO.getTravelDate())
                .title(planRequestDTO.getTitle())
                .description(planRequestDTO.getDescription())
                .startTime(planRequestDTO.getStartTime())
                .endTime(planRequestDTO.getEndTime())
                .build();

        Plan savedPlan = planRepository.save(p);

        return PlanResponseDTO.fromEntity(savedPlan);
    }

    // Plan 수정
    @Transactional
    public PlanResponseDTO update(String userId, Integer planId, PlanRequestDTO planRequestDTO) {
        Plan p = planRepository.findById(planId).orElse(null);
        p.updateFrom(planRequestDTO);

        return PlanResponseDTO.fromEntity(p);
    }

    // Plan 삭제
    public void delete(String userId, Integer planId) {
        planRepository.deleteById(planId);
    }

    // 날짜별 일정 조회
    public List<PlanResponseDTO> getByDate(String userId, LocalDate date) {
        return planRepository.findAllByUser_UserIdAndTravelDate(userId,date)
                .stream()
                .map(PlanResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
