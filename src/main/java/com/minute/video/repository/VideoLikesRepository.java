package com.minute.video.repository;

import com.minute.user.entity.User;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoLikes;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VideoLikesRepository extends JpaRepository<VideoLikes, Integer> {

    // 특정 사용자가 좋아요한 영상 목록
    List<VideoLikes> findByUserUserId(String userId);

    // 영상 좋아요 여부 확인
    boolean existsByUserUserIdAndVideoVideoId(String userId, String videoId);

    // 영상 좋아요 개수 조회
    Long countByVideoVideoId(String videoId);

    // 삭제한 행 수를 반환하도록 변경
    @Modifying
    @Transactional
    int deleteByUserUserIdAndVideoVideoId(String userId, String videoId);

}