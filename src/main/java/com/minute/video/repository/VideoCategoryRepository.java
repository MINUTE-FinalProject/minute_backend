package com.minute.video.repository;

import com.minute.video.Entity.Category;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoCategoryRepository extends JpaRepository<VideoCategory, Integer> {

    // 특정 영상에 속한 카테고리 목록
    List<VideoCategory> findByVideo(Video video);

    // 특정 카테고리에 속한 영상 목록
    List<VideoCategory> findByCategory(Category category);

    // 복합키를 통한 단일 조회
    Optional<VideoCategory> findById(VideoCategory.VideoCategoryId id);

    // 중복 데이터 방지를 위한 조회
    Optional<VideoCategory> findByVideoAndCategory(Video video, Category category);
}
