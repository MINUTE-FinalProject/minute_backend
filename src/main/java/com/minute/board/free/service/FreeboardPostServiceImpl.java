package com.minute.board.free.service; // 실제 프로젝트 구조에 맞게 패키지 경로를 수정해주세요.

import com.minute.board.common.dto.PageResponseDTO;
import com.minute.board.free.dto.response.FreeboardPostSimpleResponseDTO;
import com.minute.board.free.entity.FreeboardPost;
import com.minute.board.free.repository.FreeboardPostRepository;
import com.minute.user.entity.User; // User 엔티티 import (경로 확인 필요)
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 조회에도 필요시 readOnly=true 옵션

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션, 데이터 변경 메서드에는 @Transactional 추가
public class FreeboardPostServiceImpl implements FreeboardPostService {

    private final FreeboardPostRepository freeboardPostRepository;
    // UserRepository도 필요할 수 있습니다. (만약 User 정보가 LAZY 로딩이고, DTO 변환 시 추가 쿼리가 발생한다면 EAGER 로딩 또는 fetch join 고려)

    @Override
    public PageResponseDTO<FreeboardPostSimpleResponseDTO> getAllPosts(Pageable pageable) {
        // DB에서 게시글 목록을 Page 형태로 조회합니다.
        // N+1 문제 방지를 위해 User 정보까지 함께 조회하려면 @EntityGraph 또는 fetch join 사용 고려
        // 예: freeboardPostRepository.findAllWithUser(pageable); (Repository에 커스텀 메서드 필요)
        Page<FreeboardPost> postPage = freeboardPostRepository.findAll(pageable);

        // Page<FreeboardPost>를 List<FreeboardPostSimpleResponseDTO>로 변환합니다.
        List<FreeboardPostSimpleResponseDTO> dtoList = postPage.getContent().stream()
                .map(this::convertToSimpleDto) // 엔티티를 DTO로 변환하는 메서드 사용
                .collect(Collectors.toList());

        // PageResponseDTO로 감싸서 반환합니다.
        return PageResponseDTO.<FreeboardPostSimpleResponseDTO>builder()
                .content(dtoList)
                .currentPage(postPage.getNumber() + 1) // Page는 0부터 시작, UI는 1부터 시작 가정
                .totalPages(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .size(postPage.getSize())
                .first(postPage.isFirst())
                .last(postPage.isLast())
                .empty(postPage.isEmpty())
                .build();
    }

    /**
     * FreeboardPost 엔티티를 FreeboardPostSimpleResponseDTO로 변환합니다.
     *
     * @param post FreeboardPost 엔티티
     * @return FreeboardPostSimpleResponseDTO
     */
    private FreeboardPostSimpleResponseDTO convertToSimpleDto(FreeboardPost post) {
        User user = post.getUser(); // 연관된 User 엔티티 가져오기
        return FreeboardPostSimpleResponseDTO.builder()
                .postId(post.getPostId())
                .postTitle(post.getPostTitle())
                .postViewCount(post.getPostViewCount())
                .postLikeCount(post.getPostLikeCount())
                .postCreatedAt(post.getPostCreatedAt())
                .userId(user != null ? user.getUserId() : null) // User가 null일 경우 대비
                .userNickName(user != null ? user.getUserNickName() : "알 수 없는 사용자") // User 또는 닉네임이 null일 경우 대비
                // .commentCount(post.getComments() != null ? post.getComments().size() : 0) // 댓글 수 필요시
                .build();
    }

    // 상세 조회, 생성, 수정, 삭제 등 다른 메서드들은 여기에 구현됩니다.
}