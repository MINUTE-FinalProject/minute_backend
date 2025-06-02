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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        User user = userRepository.findById(searchRequestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + searchRequestDTO.getUserId()));

        // 항상 현재 시각을 사용
        LocalDateTime now = LocalDateTime.now();

        // 1. 개인 검색 이력 저장
        SearchHistory searchHistory = SearchHistory.builder()
                .user(user)
                .keyword(searchRequestDTO.getKeyword())
                .searchedAt(now)
                .build();
        searchHistoryRepository.save(searchHistory);

        // 2. 인기 검색어 카운트 1 증가 (없으면 새로 생성)
        PopularSearch popularSearch = popularSearchRepository.findById(searchRequestDTO.getKeyword())
                .orElseGet(()->{
                    PopularSearch ps = new PopularSearch();
                    ps.setKeyword(searchRequestDTO.getKeyword());
                    ps.setSearchCount(0);
                    return ps;
                });
        popularSearch.setSearchCount(popularSearch.getSearchCount() + 1);
        popularSearchRepository.save(popularSearch);
    }


    // 사용자의 검색 기록 조회(최신순)
    @Transactional(readOnly = true)
    public List<SearchHistoryResponseDTO> getUserSearchHistory(String userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return searchHistoryRepository.findByUserUserIdOrderBySearchedAtDesc(userId).stream()
                .map(searchHistory -> new SearchHistoryResponseDTO(
                        searchHistory.getUser().getUserId(),
                        searchHistory.getSearchId(),
                        searchHistory.getKeyword(),
                        searchHistory.getSearchedAt()))
                .collect(Collectors.toList());
    }

    // 최신 검색 키워드 (중복 제거 후 최대 5개)
    public List<String> getRecentKeywords(String userId){
        userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found: " + userId));

        return searchHistoryRepository.findByUserUserIdOrderBySearchedAtDesc(userId).stream()
                .map(SearchHistory::getKeyword)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    // 인기 검색 키워드 (상위 5개)
    public List<String> getPopularKeywords(){
        return popularSearchRepository.findTop5ByOrderBySearchCountDesc().stream()
                .map(PopularSearch::getKeyword)
                .collect(Collectors.toList());
    }

    // 검색창에 최신검색어, 인기검색어 나오게
    public SearchSuggestionsDTO getSearchSuggestions(String userId){
        List<SearchHistoryResponseDTO> recent = getUserSearchHistory(userId)
                .stream()
                .limit(5) // 최대 5개
                .collect(Collectors.toList());

        List<String> popular = getPopularKeywords();  // 상위 5개
        return new SearchSuggestionsDTO(recent,popular);
    }

    // 최근 검색어 삭제
    @Transactional
    public void deleteSearchHistory(Integer searchId) {
        searchHistoryRepository.deleteById(searchId);
    }

}
