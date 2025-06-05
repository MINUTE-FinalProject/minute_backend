package com.minute.board.notice.service;

import com.minute.board.common.dto.response.PageResponseDTO;
import com.minute.board.notice.dto.request.NoticeCreateRequestDTO;
import com.minute.board.notice.dto.request.NoticeImportanceUpdateRequestDTO;
import com.minute.board.notice.dto.request.NoticeUpdateRequestDTO;
import com.minute.board.notice.dto.response.NoticeDetailResponseDTO;
import com.minute.board.notice.dto.response.NoticeListResponseDTO;
import com.minute.board.notice.entity.Notice;
import com.minute.board.notice.repository.NoticeRepository;
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // ⭐ 추가: Slf4j 임포트
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // ⭐ 추가: Slf4j 어노테이션
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponseDTO<NoticeListResponseDTO> getNoticeList(
            Pageable pageable,
            String searchKeyword,
            Boolean isImportant,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        // ⭐ 추가: 받은 파라미터 로그 출력
        log.info("NoticeService.getNoticeList called with:");
        log.info("  searchKeyword: {}", searchKeyword);
        log.info("  isImportant: {}", isImportant);
        log.info("  dateFrom: {}", dateFrom);
        log.info("  dateTo: {}", dateTo);
        log.info("  pageable: {}", pageable); // pageable 객체는 정렬 정보도 포함합니다.

        // 1. 검색 조건 Specification 생성
        Specification<Notice> spec = Specification.where(null);

        // 1-1. 텍스트 키워드 통합 검색 조건 (searchKeyword만 사용)
        if (StringUtils.hasText(searchKeyword)) {
            // NoticeSpecification의 searchByCombinedFields 메소드를 사용하여 여러 필드 통합 검색
            spec = spec.and(NoticeSpecification.searchByCombinedFields(searchKeyword));
        }

        // 1-2. 중요도 필터 조건 추가 (새로운 로직)
        if (isImportant != null) {
            spec = spec.and(NoticeSpecification.isImportant(isImportant));
        }

        // 1-3. 날짜 범위 필터 조건 추가 (새로운 로직)
        if (dateFrom != null || dateTo != null) {
            spec = spec.and(NoticeSpecification.createdAtBetween(dateFrom, dateTo));
        }

        // 2. 레포지토리에서 데이터 조회하기 (Specification 적용)
        // Spring Data JPA의 Pageable은 자동으로 정렬 정보를 처리합니다.
        // 프론트엔드에서 sort=noticeIsImportant,desc&sort=noticeCreatedAt,desc 로 보내면
        // Pageable 객체 내부에 Sort 정보가 제대로 파싱되어 들어옵니다.
        Page<Notice> noticePage = noticeRepository.findAll(spec, pageable);

        // 3. Page<Notice> 내용을 List<NoticeListResponseDTO>로 변환하기
        List<NoticeListResponseDTO> dtoList = noticePage.getContent().stream()
                .map(notice -> NoticeListResponseDTO.builder()
                        .noticeId(notice.getNoticeId())
                        .noticeTitle(notice.getNoticeTitle())
                        .authorId(notice.getUser().getUserId())
                        .authorNickname(notice.getUser().getUserNickName())
                        .noticeCreatedAt(notice.getNoticeCreatedAt())
                        .noticeViewCount(notice.getNoticeViewCount())
                        .noticeIsImportant(notice.isNoticeIsImportant())
                        .build())
                .collect(Collectors.toList());

        // 4. PageResponseDTO 생성 및 반환
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

    // 공지사항 상세 조회 기능 관련
    @Transactional // 조회수 증가가 있으므로 readOnly = false (기본값)
    public NoticeDetailResponseDTO getNoticeDetail(Integer noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 공지사항을 찾을 수 없습니다: " + noticeId));

        notice.setNoticeViewCount(notice.getNoticeViewCount() + 1);

        return NoticeDetailResponseDTO.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .authorId(notice.getUser().getUserId())
                .authorNickname(notice.getUser().getUserNickName())
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .noticeViewCount(notice.getNoticeViewCount())
                .noticeIsImportant(notice.isNoticeIsImportant())
                .build();
    }

    // 공지사항 작성 기능 관련
    @Transactional
    public NoticeDetailResponseDTO createNotice(NoticeCreateRequestDTO requestDto, String authenticatedUserId) {
        User author = userRepository.findUserByUserId(authenticatedUserId)
                .orElseThrow(() -> new EntityNotFoundException("작성자 정보를 찾을 수 없습니다: " + authenticatedUserId));

        Notice newNotice = Notice.builder()
                .noticeTitle(requestDto.getNoticeTitle())
                .noticeContent(requestDto.getNoticeContent())
                .noticeIsImportant(requestDto.isNoticeIsImportant())
                .user(author)
                .noticeViewCount(0)
                .build();

        Notice savedNotice = noticeRepository.save(newNotice);

        return NoticeDetailResponseDTO.builder()
                .noticeId(savedNotice.getNoticeId())
                .noticeTitle(savedNotice.getNoticeTitle())
                .noticeContent(savedNotice.getNoticeContent())
                .authorId(savedNotice.getUser().getUserId())
                .authorNickname(savedNotice.getUser().getUserNickName())
                .noticeCreatedAt(savedNotice.getNoticeCreatedAt())
                .noticeViewCount(savedNotice.getNoticeViewCount())
                .noticeIsImportant(savedNotice.isNoticeIsImportant())
                .build();
    }

    // 공지사항 수정 기능 관련
    @Transactional
    public NoticeDetailResponseDTO updateNotice(Integer noticeId, NoticeUpdateRequestDTO requestDto, String authenticatedUserId) {
        Notice noticeToUpdate = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 공지사항을 찾을 수 없습니다 (ID: " + noticeId + ")"));

        // 권한 확인 (팀원 담당이라고 하셨지만, 백엔드 로직이 필요하다면 여기에 추가)
        // 예: if (!noticeToUpdate.getUser().getUserId().equals(authenticatedUserId) && !authenticatedUserHasAdminRole) {
        //         throw new AccessDeniedException("이 공지사항을 수정할 권한이 없습니다.");
        //     }

        boolean updated = false;
        if (requestDto.getNoticeTitle() != null && !requestDto.getNoticeTitle().equals(noticeToUpdate.getNoticeTitle())) {
            noticeToUpdate.setNoticeTitle(requestDto.getNoticeTitle());
            updated = true;
        }
        if (requestDto.getNoticeContent() != null && !requestDto.getNoticeContent().equals(noticeToUpdate.getNoticeContent())) {
            noticeToUpdate.setNoticeContent(requestDto.getNoticeContent());
            updated = true;
        }
        if (requestDto.getNoticeIsImportant() != null && requestDto.getNoticeIsImportant() != noticeToUpdate.isNoticeIsImportant()) {
            noticeToUpdate.setNoticeIsImportant(requestDto.getNoticeIsImportant());
            updated = true;
        }

        return NoticeDetailResponseDTO.builder()
                .noticeId(noticeToUpdate.getNoticeId())
                .noticeTitle(noticeToUpdate.getNoticeTitle())
                .noticeContent(noticeToUpdate.getNoticeContent())
                .authorId(noticeToUpdate.getUser().getUserId())
                .authorNickname(noticeToUpdate.getUser().getUserNickName())
                .noticeCreatedAt(noticeToUpdate.getNoticeCreatedAt())
                .noticeViewCount(noticeToUpdate.getNoticeViewCount())
                .noticeIsImportant(noticeToUpdate.isNoticeIsImportant())
                .build();
    }

    // 공지사항 삭제 기능 관련
    @Transactional
    public void deleteNotice(Integer noticeId, String authenticatedUserId) {
        Notice noticeToDelete = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 공지사항을 찾을 수 없습니다 (ID: " + noticeId + ")"));

        // 권한 확인 (팀원 담당이라고 하셨지만, 백엔드 로직이 필요하다면 여기에 추가)
        // 예: if (!authenticatedUserHasAdminRole && !noticeToDelete.getUser().getUserId().equals(authenticatedUserId)) {
        //         throw new AccessDeniedException("이 공지사항을 삭제할 권한이 없습니다.");
        //     }

        noticeRepository.delete(noticeToDelete);
    }

    // 공지사항 중요도 변경 기능 관련
    @Transactional
    public NoticeDetailResponseDTO updateNoticeImportance(Integer noticeId, NoticeImportanceUpdateRequestDTO requestDto, String authenticatedUserId) {
        Notice noticeToUpdate = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("중요도를 변경할 공지사항을 찾을 수 없습니다 (ID: " + noticeId + ")"));

        // 권한 확인 (팀원 담당이라고 하셨지만, 백엔드 로직이 필요하다면 여기에 추가)
        // 예: if (!authenticatedUserHasAdminRole) {
        //         throw new AccessDeniedException("이 공지사항의 중요도를 변경할 권한이 없습니다.");
        //     }

        noticeToUpdate.setNoticeIsImportant(requestDto.getNoticeIsImportant());

        return NoticeDetailResponseDTO.builder()
                .noticeId(noticeToUpdate.getNoticeId())
                .noticeTitle(noticeToUpdate.getNoticeTitle())
                .noticeContent(noticeToUpdate.getNoticeContent())
                .authorId(noticeToUpdate.getUser().getUserId())
                .authorNickname(noticeToUpdate.getUser().getUserNickName())
                .noticeCreatedAt(noticeToUpdate.getNoticeCreatedAt())
                .noticeViewCount(noticeToUpdate.getNoticeViewCount())
                .noticeIsImportant(noticeToUpdate.isNoticeIsImportant())
                .build();
    }
}