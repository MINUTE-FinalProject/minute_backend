package com.minute.video.repository;

import com.minute.video.Entity.PopularSearch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PopularSearchRepository extends JpaRepository<PopularSearch, Integer> {

    // 인기 검색어
    List<PopularSearch> findTop10ByOrderByCountDesc();

    // 특정 검색어가 있는지 확인
    boolean existsByKeyword(String keyword);
}
