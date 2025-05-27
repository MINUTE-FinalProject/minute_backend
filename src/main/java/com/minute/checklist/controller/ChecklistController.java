package com.minute.checklist.controller;

import com.minute.checklist.dto.request.ChecklistRequestDTO;
import com.minute.checklist.dto.response.ChecklistResponseDTO;
import com.minute.checklist.service.ChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.List;

@Tag(name = "Checklist", description = "Checklist 관련 API")
@RestController
@RequestMapping("/api/v1/checklists")
@RequiredArgsConstructor
public class ChecklistController {
    private final ChecklistService checklistService;

    @Operation(description = "특정 날짜 체크리스트 조회")
    @GetMapping
    public List<ChecklistResponseDTO> getByDate(
            Principal principal,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        String userId = principal.getName();
        return checklistService.getChecklistsByUserAndDate(userId, date);
    }

    @Operation(summary = "체크리스트 항목 추가")
    @PostMapping
    public ChecklistResponseDTO create(
            Principal principal,
            @RequestBody @Valid ChecklistRequestDTO dto) {
        return checklistService.create(principal.getName(), dto);
    }

    @Operation(summary = "체크리스트 항목 수정")
    @PutMapping("/{checklistId}")
    public ChecklistResponseDTO update(
            Principal principal,
            @PathVariable Integer checklistId,
            @RequestBody @Valid ChecklistRequestDTO dto) {
        return checklistService.update(principal.getName(), checklistId, dto);
    }

    @Operation(summary = "체크리스트 항목 삭제")
    @DeleteMapping("/{checklistId}")
    public void delete(
            Principal principal,
            @PathVariable Integer checklistId) {
        checklistService.delete(principal.getName(), checklistId);
    }
}
