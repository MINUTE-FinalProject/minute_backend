package com.minute.video.repository;

import com.minute.user.entity.User;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Integer> {

    // 특정 사용자의 시청 기록 조회 (최신순)
    List<WatchHistory> findByUserUserIdOrderByWatchedAtDesc(String userId);

    // 조회수(WatchHistory 레코드 수) 기준 인기 영상 TOP50
    @Query("""
        SELECT wh.video 
        FROM WatchHistory wh 
        GROUP BY wh.video 
        ORDER BY COUNT(wh) DESC 
        """)
    List<Video> findMostWatchedVideos();
}
