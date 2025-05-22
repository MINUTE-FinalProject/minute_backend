package com.minute.board.notice.service;

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.notice.dto.request.NoticeCreateRequestDTO;
import com.minute.board.notice.dto.response.NoticeDetailResponseDTO;
import com.minute.board.notice.dto.response.NoticeListResponseDTO;
import com.minute.board.notice.entity.Notice;
import com.minute.board.notice.repository.NoticeRepository;
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException; // 표준 예외 사용 가능
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
    private final UserRepository userRepository;

    // 전체 목록 조회 기능 관련

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

    // 공지사항 상세 조회 기능 관련

    @Transactional // 조회수 증가가 있으므로 readOnly = false (기본값)
    public NoticeDetailResponseDTO getNoticeDetail(Integer noticeId) {
        // 1. noticeId로 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 공지사항을 찾을 수 없습니다: " + noticeId));
        // 또는 커스텀 예외 사용: .orElseThrow(() -> new ResourceNotFoundException("Notice", "id", noticeId));

        // 2. 조회수 증가 (단순 증가 로직)
        notice.setNoticeViewCount(notice.getNoticeViewCount() + 1);
        // noticeRepository.save(notice); // JPA의 영속성 컨텍스트 'dirty checking'으로 인해 @Transactional 범위 내에서는 명시적 save 호출이 필수는 아닐 수 있으나,
        // 명확성을 위해 또는 특정 상황(예: 트랜잭션 전파 옵션)에서는 필요할 수 있습니다.
        // 일반적으로 변경 감지가 동작하여 트랜잭션 커밋 시 업데이트됩니다.

        // 3. Notice 엔티티를 NoticeDetailResponseDTO로 변환
        return NoticeDetailResponseDTO.builder()
                .noticeId(notice.getNoticeId())
                .noticeTitle(notice.getNoticeTitle())
                .noticeContent(notice.getNoticeContent())
                .authorId(notice.getUser().getUserId())
                .authorNickname(notice.getUser().getUserNickName())
                .noticeCreatedAt(notice.getNoticeCreatedAt())
                .noticeViewCount(notice.getNoticeViewCount()) // 증가된 조회수
                .noticeIsImportant(notice.isNoticeIsImportant())
                .build();
    }

    // 공지사항 작성 기능 관련

    @Transactional // 데이터 변경이 있으므로 @Transactional 추가
    public NoticeDetailResponseDTO createNotice(NoticeCreateRequestDTO requestDto, String authenticatedUserId) {
        // 1. 작성자(User) 정보 조회
        User author = userRepository.findUserByUserId(authenticatedUserId);
        if (author == null) {
            // 또는 findByUserId가 Optional<User>를 반환한다면 .orElseThrow() 사용
            throw new EntityNotFoundException("작성자 정보를 찾을 수 없습니다: " + authenticatedUserId);
        }

        // 2. Notice 엔티티 생성 및 정보 설정
        Notice newNotice = Notice.builder()
                .noticeTitle(requestDto.getNoticeTitle())
                .noticeContent(requestDto.getNoticeContent())
                .noticeIsImportant(requestDto.isNoticeIsImportant())
                .user(author) // 작성자 엔티티 설정
                .noticeViewCount(0) // 초기 조회수는 0
                // noticeCreatedAt, noticeUpdatedAt는 Notice 엔티티의 @CreationTimestamp, @UpdateTimestamp에 의해 자동 설정됨
                .build();

        // 3. 생성된 Notice 엔티티 저장
        Notice savedNotice = noticeRepository.save(newNotice);

        // 4. 저장된 Notice 엔티티를 NoticeDetailResponseDTO로 변환하여 반환
        //    (방금 만든 공지사항의 상세 정보를 바로 보여주기 위함)
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
}