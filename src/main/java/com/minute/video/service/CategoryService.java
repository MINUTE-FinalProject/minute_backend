package com.minute.video.service;

import com.minute.video.dto.CategoryDTO;
import com.minute.video.dto.VideoResponseDTO;
import com.minute.video.mapper.VideoMapper;
import com.minute.video.repository.CategoryRepository;
import com.minute.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    // 카테고리 조회 및 카테고리 기반 영상 조회

    private final CategoryRepository categoryRepository;

    // 모든 카테고리 조회
    public List<CategoryDTO> getCategoryList() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryDTO(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getYoutubeKeyword()))
                .collect(Collectors.toList());
    }

}
