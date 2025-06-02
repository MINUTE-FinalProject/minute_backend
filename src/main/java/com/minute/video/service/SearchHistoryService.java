package com.minute.video.service;

import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.video.Entity.PopularSearch;
import com.minute.video.Entity.SearchHistory;
import com.minute.video.dto.SearchHistoryRequestDTO;
import com.minute.video.dto.SearchHistoryResponseDTO;
import com.minute.video.dto.SearchSuggestionsDTO;
import com.minute.video.repository.PopularSearchRepository;
import com.minute.video.repository.SearchHistoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;
    private final PopularSearchRepository popularSearchRepository;

    // 사용자의 검색어 저장 + 인기 검색어 집계
    @Transactional
    public void saveSearchHistory(SearchHistoryRequestDTO searchRequestDTO) {
        User user = null;

        // userId가 존재하고 DB에 있을 경우에만 개인 검색 기록 저장
        if (searchRequestDTO.getUserId() != null && !searchRequestDTO.getUserId().isBlank()) {
            user = userRepository.findById(searchRequestDTO.getUserId()).orElse(null);
        }

        LocalDateTime now = LocalDateTime.now();

        if (user != null) {
            // 1. 개인 검색 이력 저장
            SearchHistory searchHistory = SearchHistory.builder()
                    .user(user)
                    .keyword(searchRequestDTO.getKeyword())
                    .searchedAt(now)
                    .build();
            searchHistoryRepository.save(searchHistory);
        }

        // 2. 인기 검색어 카운트 1 증가 (없으면 새로 생성)
        PopularSearch popularSearch = popularSearchRepository.findById(searchRequestDTO.getKeyword())
                .orElseGet(() -> {
                    PopularSearch ps = new PopularSearch();
                    ps.setKeyword(searchRequestDTO.getKeyword());
                    ps.setSearchCount(0);
                    return ps;
                });
        popularSearch.setSearchCount(popularSearch.getSearchCount() + 1);
        popularSearch.setUpdatedAt(now);
        popularSearchRepository.save(popularSearch);
    }

    // 사용자의 검색 기록 조회(최신순)
    @Transactional(readOnly = true)
    public List<SearchHistoryResponseDTO> getUserSearchHistory(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }

        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            return Collections.emptyList();
        }

        return searchHistoryRepository.findByUserUserIdOrderBySearchedAtDesc(userId).stream()
                .map(searchHistory -> new SearchHistoryResponseDTO(
                        searchHistory.getUser().getUserId(),
                        searchHistory.getSearchId(),
                        searchHistory.getKeyword(),
                        searchHistory.getSearchedAt()))
                .collect(Collectors.toList());
    }

    // 최신 검색 키워드 (중복 제거 후 최대 5개)
    public List<String> getRecentKeywords(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }

        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            return Collections.emptyList();
        }

        return searchHistoryRepository.findByUserUserIdOrderBySearchedAtDesc(userId).stream()
                .map(SearchHistory::getKeyword)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    // 인기 검색 키워드 (상위 5개)
    public List<String> getPopularKeywords() {
        return popularSearchRepository.findTop5ByOrderBySearchCountDesc().stream()
                .map(PopularSearch::getKeyword)
                .collect(Collectors.toList());
    }

    // 검색창에 최신검색어, 인기검색어 나오게
    public SearchSuggestionsDTO getSearchSuggestions(String userId) {
        List<SearchHistoryResponseDTO> recent = getUserSearchHistory(userId).stream()
                .limit(5)
                .collect(Collectors.toList());

        List<String> popular = getPopularKeywords();
        return new SearchSuggestionsDTO(recent, popular);
    }

    // 최근 검색어 삭제
    @Transactional
    public void deleteSearchHistory(Integer searchId) {
        searchHistoryRepository.deleteById(searchId);
    }
}
