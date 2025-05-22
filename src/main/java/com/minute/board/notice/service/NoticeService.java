package com.minute.board.notice.service;

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.notice.dto.request.NoticeCreateRequestDTO;
import com.minute.board.notice.dto.request.NoticeImportanceUpdateRequestDTO;
import com.minute.board.notice.dto.request.NoticeUpdateRequestDTO;
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

    // 공지사항 수정 기능 관련

    @Transactional // 데이터 변경이 있으므로 @Transactional 추가
    public NoticeDetailResponseDTO updateNotice(Integer noticeId, NoticeUpdateRequestDTO requestDto, String authenticatedUserId) {
        // 1. 수정할 공지사항 조회
        Notice noticeToUpdate = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 공지사항을 찾을 수 없습니다 (ID: " + noticeId + ")"));

        // 2. 권한 확인 (임시: 현재는 이 로직이 완전하지 않음. ADMIN 역할 확인은 SecurityContext에서 이루어져야 함)
        //    - 실제로는 authenticatedUserId가 ADMIN 역할을 가졌는지,
        //    - 또는 이 공지사항의 작성자인지 등을 확인해야 합니다.
        //    - 지금은 ADMIN 역할 검사는 SecurityConfig에서 경로별로 하고,
        //      여기서는 일단 특정 사용자만 수정 가능하게 하려면 noticeToUpdate.getUser().getUserId().equals(authenticatedUserId) 같은 조건을 추가할 수 있습니다.
        //    - 팀원분의 인증/인가 기능이 완성되면 이 부분을 강화해야 합니다.
        //    - 예시: if (!noticeToUpdate.getUser().getUserId().equals(authenticatedUserId) && !/* authenticatedUserHasAdminRole */) {
        //                throw new AccessDeniedException("이 공지사항을 수정할 권한이 없습니다.");
        //            }


        // 3. DTO로부터 받은 값으로 공지사항 정보 업데이트 (null이 아닌 필드만)
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

        // 4. 실제로 변경된 사항이 있을 경우에만 저장 (선택적: @UpdateTimestamp 때문에 어차피 업데이트될 수 있음)
        //    JPA의 Dirty Checking 기능으로 인해 @Transactional 범위 내에서는 변경된 엔티티가 자동으로 DB에 반영됩니다.
        //    따라서, 명시적인 save 호출은 필수는 아니지만, updated 플래그를 통해 실제로 변경이 있었는지 로깅하거나
        //    다른 처리를 할 때 유용할 수 있습니다. @UpdateTimestamp는 필드 값 변경 여부와 관계없이 엔티티가 persist/merge 될 때 갱신될 수 있습니다.
        //    정확한 동작은 @UpdateTimestamp의 구현과 JPA provider에 따라 다를 수 있으므로,
        //    변경이 있을 때만 save를 호출하거나, 항상 save를 호출하는 것 중 선택할 수 있습니다.
        //    여기서는 변경이 없으면 굳이 save를 호출하지 않는 로직은 생략하고, dirty checking에 의존합니다.
        //    만약 @UpdateTimestamp가 항상 갱신되길 원치 않는다면, updated 플래그를 활용하세요.

        // noticeRepository.save(noticeToUpdate); // 명시적으로 호출하거나 Dirty Checking에 의존

        // 5. 업데이트된 Notice 엔티티를 NoticeDetailResponseDTO로 변환하여 반환
        return NoticeDetailResponseDTO.builder()
                .noticeId(noticeToUpdate.getNoticeId())
                .noticeTitle(noticeToUpdate.getNoticeTitle())
                .noticeContent(noticeToUpdate.getNoticeContent())
                .authorId(noticeToUpdate.getUser().getUserId())
                .authorNickname(noticeToUpdate.getUser().getUserNickName())
                .noticeCreatedAt(noticeToUpdate.getNoticeCreatedAt()) // 생성일은 변경되지 않음
                .noticeViewCount(noticeToUpdate.getNoticeViewCount()) // 조회수는 변경되지 않음
                .noticeIsImportant(noticeToUpdate.isNoticeIsImportant())
                .build();
    }

    // 공지사항 삭제 기능 관련

    @Transactional // 데이터 변경(삭제)이 있으므로 @Transactional 추가
    public void deleteNotice(Integer noticeId, String authenticatedUserId) {
        // 1. 삭제할 공지사항 조회 (존재 여부 확인)
        Notice noticeToDelete = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 공지사항을 찾을 수 없습니다 (ID: " + noticeId + ")"));

        // 2. 권한 확인 (임시: 현재는 이 로직이 완전하지 않음)
        //    - 실제로는 authenticatedUserId가 ADMIN 역할을 가졌는지,
        //    - 또는 특정 조건(예: 본인 게시물)을 만족하는지 등을 확인해야 합니다.
        //    - 팀원분의 인증/인가 기능이 완성되면 이 부분을 강화해야 합니다.
        //    - 예시: if (!/* authenticatedUserHasAdminRole */ && !noticeToDelete.getUser().getUserId().equals(authenticatedUserId)) {
        //                throw new AccessDeniedException("이 공지사항을 삭제할 권한이 없습니다.");
        //            }

        // 3. 공지사항 삭제
        noticeRepository.delete(noticeToDelete);
        // 또는 noticeRepository.deleteById(noticeId); 를 사용할 수 있습니다.
        // deleteById의 경우, 해당 ID의 엔티티가 없으면 EmptyResultDataAccessException이 발생할 수 있으므로,
        // findById로 먼저 조회 후 delete(entity)를 하는 것이 조금 더 안전하거나, deleteById의 예외를 처리할 수 있습니다.
        // 여기서는 이미 findById로 조회했으므로 delete(entity)를 사용합니다.
    }

    // 공지사항 중요도 변경 기능 관련

    @Transactional // 데이터 변경(수정)이 있으므로 @Transactional 추가
    public NoticeDetailResponseDTO updateNoticeImportance(Integer noticeId, NoticeImportanceUpdateRequestDTO requestDto, String authenticatedUserId) {
        // 1. 수정할 공지사항 조회
        Notice noticeToUpdate = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("중요도를 변경할 공지사항을 찾을 수 없습니다 (ID: " + noticeId + ")"));

        // 2. 권한 확인 (임시: 현재는 이 로직이 완전하지 않음)
        //    - 실제로는 authenticatedUserId가 ADMIN 역할을 가졌는지 확인해야 합니다.
        //    - 팀원분의 인증/인가 기능이 완성되면 이 부분을 강화해야 합니다.
        //    - 예시: if (!/* authenticatedUserHasAdminRole */) {
        //                throw new AccessDeniedException("이 공지사항의 중요도를 변경할 권한이 없습니다.");
        //            }

        // 3. DTO로부터 받은 값으로 공지사항 중요도 업데이트
        // requestDto.getNoticeIsImportant()는 @NotNull이므로 null이 아님을 보장받습니다.
        noticeToUpdate.setNoticeIsImportant(requestDto.getNoticeIsImportant());

        // 4. 변경 사항 저장 (JPA의 Dirty Checking 기능으로 @Transactional 범위 내에서 자동 반영)
        // noticeRepository.save(noticeToUpdate); // 명시적으로 호출해도 괜찮습니다.

        // 5. 업데이트된 Notice 엔티티를 NoticeDetailResponseDTO로 변환하여 반환
        //    (클라이언트가 변경된 전체 상태를 확인할 수 있도록)
        return NoticeDetailResponseDTO.builder()
                .noticeId(noticeToUpdate.getNoticeId())
                .noticeTitle(noticeToUpdate.getNoticeTitle())
                .noticeContent(noticeToUpdate.getNoticeContent())
                .authorId(noticeToUpdate.getUser().getUserId())
                .authorNickname(noticeToUpdate.getUser().getUserNickName())
                .noticeCreatedAt(noticeToUpdate.getNoticeCreatedAt())
                .noticeViewCount(noticeToUpdate.getNoticeViewCount()) // 조회수는 이 작업으로 변경되지 않음
                .noticeIsImportant(noticeToUpdate.isNoticeIsImportant()) // 변경된 중요도
                .build();
    }
}