package com.minute.board.notice.service;

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.notice.dto.NoticeListResponseDTO;
import com.minute.board.notice.entity.Notice;
import com.minute.board.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor; // 생성자 주입을 위해 사용
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 선택 사항 (읽기 전용 트랜잭션)

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok: final 필드에 대한 생성자를 만들어줍니다.
public class NoticeService {

    private final NoticeRepository noticeRepository; // NoticeRepository 의존성 주입

    @Transactional(readOnly = true) // 읽기 작업에 대한 좋은 습관입니다.
    public PageResponseDTO<NoticeListResponseDTO> getNoticeList(Pageable pageable) {
        // 1. 레포지토리에서 데이터 조회하기
        //    Pageable 객체는 이상적으로 이미 정렬 설정(예: 중요 공지 우선, 그 다음 작성일 순)을 포함해야 합니다.
        //    그렇지 않다면, 여기서 특정 Sort 객체를 포함한 PageRequest를 새로 만들어야 할 수 있습니다.
        Page<Notice> noticePage = noticeRepository.findAll(pageable);

        // 2. Page<Notice> 내용을 List<NoticeListResponseDTO>로 변환하기
        List<NoticeListResponseDTO> dtoList = noticePage.getContent().stream()
                .map(notice -> NoticeListResponseDTO.builder()
                        .noticeId(notice.getNoticeId())
                        .noticeTitle(notice.getNoticeTitle())
                        .authorId(notice.getUser().getUserId()) // User 엔티티가 올바르게 매핑되어 있다고 가정
                        .authorNickname(notice.getUser().getUserNickName()) // User 엔티티가 올바르게 매핑되어 있다고 가정
                        .noticeCreatedAt(notice.getNoticeCreatedAt())
                        .noticeViewCount(notice.getNoticeViewCount())
                        .noticeIsImportant(notice.isNoticeIsImportant())
                        .build())
                .collect(Collectors.toList());

        // 3. PageResponseDTO 생성 및 반환
        return PageResponseDTO.<NoticeListResponseDTO>builder()
                .content(dtoList)
                .currentPage(noticePage.getNumber() + 1) // 0-based를 1-based로 변환
                .totalPages(noticePage.getTotalPages())
                .totalElements(noticePage.getTotalElements())
                .size(noticePage.getSize())
                .first(noticePage.isFirst())
                .last(noticePage.isLast())
                .empty(noticePage.isEmpty())
                .build();
    }
}