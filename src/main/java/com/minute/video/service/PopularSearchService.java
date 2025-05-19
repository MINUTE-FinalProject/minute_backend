package com.minute.video.service;

import com.minute.video.Entity.PopularSearch;
import com.minute.video.repository.PopularSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PopularSearchService {
    // 인기 검색어 조회 및 관리

    private final PopularSearchRepository popularSearchRepository;

    // 인기 검색어 조회
    public List<String> getPopularSearches() {
        return popularSearchRepository.findTop10ByOrderByCountDesc().stream()
                .map(PopularSearch::getKeyword)
                .collect(Collectors.toList());
    }
}
