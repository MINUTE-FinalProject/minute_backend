package com.minute.video.service;

import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.video.Entity.SearchHistory;
import com.minute.video.dto.SearchHistoryRequestDTO;
import com.minute.video.dto.SearchHistoryResponseDTO;
import com.minute.video.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    // 검색 기록 저장 및 조회

    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    // 검색기록 저장
    public void saveSearchHistory(SearchHistoryRequestDTO searchRequestDTO) {
        // userId를 사용하여 User 객체를 먼저 조회
        User user = userRepository.findById(searchRequestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + searchRequestDTO.getUserId()));

        SearchHistory searchHistory = SearchHistory.builder()
                .user(user)
                .keyword(searchRequestDTO.getKeyword())
                .searchedAt(searchRequestDTO.getSearchedAt())
                .build();
        searchHistoryRepository.save(searchHistory);
    }

    // 특정 사용자의 검색 기록 조회
    public List<SearchHistoryResponseDTO> getUserSearchHistory(String userId) {
        // userId를 사용하여 User 객체를 먼저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userId).stream()
                .map(searchHistory -> new SearchHistoryResponseDTO(
                        searchHistory.getUser().getUserId(),
                        searchHistory.getKeyword(),
                        searchHistory.getSearchedAt()))
                .collect(Collectors.toList());

    }
}
