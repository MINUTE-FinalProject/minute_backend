package com.minute.board.free.dto.response; // 또는 admin용 DTO 패키지를 만들어도 좋습니다. (예: com.minute.board.free.dto.admin.response)

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor; // DTO 프로젝션을 위해 기본 생성자가 필요할 수 있습니다.
// import lombok.AllArgsConstructor; // 모든 필드를 받는 생성자는 JPA DTO 프로젝션 시 필요합니다.

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor // JPA DTO 프로젝션을 위해 기본 생성자가 필요할 수 있음
@Schema(description = "신고된 게시글 목록의 각 항목 정보 DTO")
public class ReportedPostEntryDTO {

    @Schema(description = "게시글 ID", example = "2")
    private Integer postId;

    @Schema(description = "게시글 제목", example = "문제가 있는 게시글 제목")
    private String postTitle;

    @Schema(description = "게시글 작성자 User ID", example = "wansu00")
    private String authorUserId;

    @Schema(description = "게시글 작성자 닉네임", example = "완수최고")
    private String authorNickname;

    @Schema(description = "게시글 작성일시", example = "2025-05-24T10:30:00")
    private LocalDateTime postCreatedAt;

    @Schema(description = "해당 게시글의 총 신고 횟수", example = "5")
    private Long reportCount; // COUNT 결과는 Long 타입일 수 있음

    @Schema(description = "게시글 숨김 처리 여부", example = "false")
    private boolean isHidden;

    // JPA DTO 프로젝션을 위한 생성자 (SELECT NEW ... 사용 시)
    public ReportedPostEntryDTO(Integer postId, String postTitle, String authorUserId, String authorNickname, LocalDateTime postCreatedAt, Long reportCount, boolean isHidden) {
        this.postId = postId;
        this.postTitle = postTitle;
        this.authorUserId = authorUserId;
        this.authorNickname = authorNickname;
        this.postCreatedAt = postCreatedAt;
        this.reportCount = reportCount;
        this.isHidden = isHidden;
    }
}