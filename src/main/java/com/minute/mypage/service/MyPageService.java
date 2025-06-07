package com.minute.mypage.service;

import com.minute.board.notice.repository.NoticeRepository;
import com.minute.board.qna.repository.QnaRepository;
import com.minute.checklist.service.ChecklistService;
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

    private final QnaRepository qnaRepository;         // <<< Repository 의존성 주입
    private final NoticeRepository noticeRepository;   // <<< Repository 의존성 주입

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

    // plans API
    public List<PlanResponseDTO> getPlansOnly(String userId, LocalDate date) {
        return planService.getPlansByUserAndDate(userId, date);
    }

    // --- 🚨 새로운 메서드 추가 ---
    /**
     * 전체 문의(QnA) 수를 조회합니다.
     * @return 전체 문의 수
     */
    public long getQnaCount() {
        return qnaRepository.count();
    }

    /**
     * 전체 공지사항 수를 조회합니다.
     * @return 전체 공지사항 수
     */
    public long getNoticeCount() {
        return noticeRepository.count();
    }
    // --- 🚨 추가 끝 ---
}
