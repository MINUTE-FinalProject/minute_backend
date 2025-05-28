package com.minute.video.repository;

import com.minute.video.Entity.VideoDislike;
import org.springframework.data.jpa.repository.JpaRepository;
public interface VideoDisLikeRepository extends JpaRepository<VideoDislike, Long>{
    boolean existsByUserUserIdAndVideoVideoId(String userId, String videoId);
    void deleteByUserUserIdAndVideoVideoId(String userId, String videoId);
}
