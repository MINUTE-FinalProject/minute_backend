package com.minute.video.repository;

import com.minute.video.Entity.Category;
import com.minute.video.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, String> {

    // 카테고리 필터링
    List<Video> findByCategoriesName(String categoryName);

    // 제목이나 설명으로 검색
    List<Video> findByTitleContainingOrDescriptionContaining(String videoTitle, String videoDescription);

    // 태그 필터링
    List<Video> findByTagsName(String tagName);

    // 최신 영상 조회
    List<Video> findTop50ByOrderByUploadDateDesc();

    // 인기순
    List<Video> findTop10ByOrderByLikesDesc();
}
