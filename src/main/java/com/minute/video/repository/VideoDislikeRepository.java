package com.minute.video.repository;

import com.minute.video.Entity.VideoDislike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoDislikeRepository extends JpaRepository<VideoDislike, Long> {
    boolean existsByUserUserIdAndVideoVideoId(String userId, String videoId);

    // 삭제된 행 수를 반환 → 성공 여부 판단에 유리
    int deleteByUserUserIdAndVideoVideoId(String userId, String videoId);

    @Query("SELECT d FROM VideoDislike d JOIN FETCH d.video WHERE d.user.userId = :userId")
    List<VideoDislike> findByUserUserId(@Param("userId") String userId);
}