package com.minute.video.repository;

import com.minute.video.Entity.PopularSearch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PopularSearchRepository extends JpaRepository<PopularSearch, String> {

    // 상위 5개 키워드 조회
    List<PopularSearch> findTop5ByOrderBySearchCountDesc();
}
