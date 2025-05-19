package com.minute.video.repository;

import com.minute.user.entity.User;
import com.minute.video.Entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Integer> {

    // 특정 사용자의 최근 검색 기록 조회 (최신순)
    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(String userId);

    // 특정 검색어가 얼마나 자주 검색되었는지 조회
    Long countByKeyword(String keyword);

}
