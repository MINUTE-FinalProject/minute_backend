package com.minute.video.repository;

import com.minute.user.entity.User;
import com.minute.video.Entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Integer> {

    // 사용자의 최근 검색 기록 조회 (최신순)
    List<SearchHistory> findByUserUserIdOrderBySearchedAtDesc(String userId);

    // 최근 검색어 삭제
    void deleteByUserUserIdAndKeyword(String userId, String keyword);
}
