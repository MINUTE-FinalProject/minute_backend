package com.minute.video.repository;

import com.minute.user.entity.User;
import com.minute.video.Entity.RecommendationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Integer> {

    // 특정 사용자의 추천 기록 조회 (관리자 대시보드)
    List<RecommendationLog> findByUserUserId(String userId);

}
