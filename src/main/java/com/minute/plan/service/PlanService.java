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
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    /** 한 달치 dot 조회용 */
    @Transactional(readOnly = true)
    public List<LocalDate> getPlanDatesInMonth(String userId, YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        // Repo 에는 userId, travelDate 사이 날짜 리스트를 반환하도록 쿼리 정의
        return planRepository.findTravelDatesInMonth(userId, start, end);
    }

    /** 특정 날짜의 Plan → DTO 매핑 */
    @Transactional(readOnly = true)
    public List<PlanResponseDTO> getPlansByUserAndDate(String userId, LocalDate date) {
        return planRepository
                .findAllByUser_UserIdAndTravelDate(userId, date)
                .stream()
                .map(PlanResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** CalendarController 에서 사용될 조회 메서드 */
    public List<PlanResponseDTO> getPlansForCalendar(String userId, LocalDate date) {
        return getPlansByUserAndDate(userId, date);
    }

    /** Plan 생성 */
    @Transactional
    public PlanResponseDTO create(String userId, PlanRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 사용자입니다: " + userId));

        Plan p = Plan.builder()
                .user(user)
                .travelDate(dto.getTravelDate())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();

        Plan saved = planRepository.save(p);
        return PlanResponseDTO.fromEntity(saved);
    }

    /** Plan 수정 */
    @Transactional
    public PlanResponseDTO update(String userId, Integer planId, PlanRequestDTO dto) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("없는 Plan입니다: " + planId));

        if (!plan.getUser().getUserId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        // 엔티티에 정의된 updateFrom() 사용
        plan.updateFrom(dto);

        return PlanResponseDTO.fromEntity(plan);
    }


    /** Plan 삭제 */
    @Transactional
    public void delete(String userId, Integer planId) {
        Plan p = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("없는 Plan입니다: " + planId));

        if (!p.getUser().getUserId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        planRepository.delete(p);
    }
}
