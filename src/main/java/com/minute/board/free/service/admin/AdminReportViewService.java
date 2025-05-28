package com.minute.board.free.service.admin; // 예시 패키지

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.free.dto.request.AdminReportFilterDTO;
import com.minute.board.free.dto.response.AdminReportedActivityItemDTO; // 위에서 정의한 DTO
import org.springframework.data.domain.Pageable;

public interface AdminReportViewService {

    /**
     * 신고된 모든 활동(게시글 신고, 댓글 신고) 내역을 통합하여 최신순으로 페이징 조회합니다.
     *
     * @param pageable 페이징 정보 (정렬 기준은 주로 reportCreatedAt)
     * @return 페이징된 신고 활동 목록
     */
    PageResponseDTO<AdminReportedActivityItemDTO> getAllReportedActivities(
            AdminReportFilterDTO filter, // <<< filter 파라미터 추가
            Pageable pageable
    );
}