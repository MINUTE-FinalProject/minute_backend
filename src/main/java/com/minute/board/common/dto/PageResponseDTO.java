package com.minute.board.common.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Schema 임포트
import lombok.Getter;
import lombok.Builder;
import java.util.List;

@Schema(description = "페이징 처리된 API 응답 DTO") // 클래스에 대한 설명
@Getter
public class PageResponseDTO<T> {

    @Schema(description = "현재 페이지에 보여줄 실제 데이터 목록")
    private final List<T> content;

    @Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
    private final int currentPage;

    @Schema(description = "전체 페이지 수", example = "10")
    private final int totalPages;

    @Schema(description = "전체 데이터(게시글) 수", example = "98")
    private final long totalElements;

    @Schema(description = "한 페이지에 보여줄 데이터 수", example = "10")
    private final int size;

    @Schema(description = "첫 페이지 여부", example = "true")
    private final boolean first;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private final boolean last;

    @Schema(description = "현재 페이지가 비어있는지 여부", example = "false")
    private final boolean empty;

    @Builder
    public PageResponseDTO(List<T> content, int currentPage, int totalPages, long totalElements, int size, boolean first, boolean last, boolean empty) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
        this.first = first;
        this.last = last;
        this.empty = empty;
    }
}