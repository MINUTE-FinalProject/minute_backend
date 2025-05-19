package com.minute.video.repository;

import com.minute.user.entity.User;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Integer> {

    // 특정 사용자의 시청 기록 조회 (최신순)
    List<WatchHistory> findByUserIdOrderByWatchedAtDesc(String userId);

    // 특정 영상의 시청자 수 조회
    Long countByVideo(Video video);
}
