package com.minute.video.repository;

import com.minute.user.entity.User;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VideoLikesRepository extends JpaRepository<VideoLikes, Integer> {

    // 특정 사용자가 좋아요한 영상 목록
    List<VideoLikes> findByUserUserId(String userId);

    // 영상 좋아요 여부 확인
    boolean existsByUserAndVideo(User user, Video video);

    // 영상 좋아요 개수 조회
    Long countByVideo(Video video);

    @Query("""
      SELECT vl.video
      FROM VideoLikes vl
      GROUP BY vl.video
      ORDER BY COUNT(vl) DESC
      """)
    List<Video> findMostLikedVideos();
}
