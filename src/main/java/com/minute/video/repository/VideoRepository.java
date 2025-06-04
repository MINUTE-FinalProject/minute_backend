package com.minute.video.repository;

import com.minute.video.Entity.Category;
import com.minute.video.Entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, String> {

    // 카테고리 필터링
    @Query("SELECT v FROM Video v JOIN v.videoCategories vc JOIN vc.category c WHERE c.categoryName = :categoryName")
    List<Video> findByCategoryName(@Param("categoryName") String categoryName);

    // 태그 필터링
    @Query("SELECT v FROM Video v JOIN v.videoTags vt JOIN vt.tag t WHERE t.tagName = :tagName")
    List<Video> findByTagName(@Param("tagName") String tagName);

    // 제목에 키워드가 포함된 영상 조회
    List<Video> findByVideoTitleContainingIgnoreCase(String keyword);

    // 영상 ID를 기준으로 최신순 정렬
    List<Video> findTop50ByOrderByVideoIdDesc();

    // 조회수 순
    List<Video> findTop50ByOrderByViewsDesc();

    // 좋아요 순
    List<Video> findTop50ByOrderByLikesDesc();

    List<Video> findByRegion(String region);

    List<Video> findByRegionAndCity(String region, String city);

    @Query("""
    SELECT v FROM Video v 
    WHERE LOWER(v.videoTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(v.region) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(v.city) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Video> searchByTitleOrRegionOrCity(@Param("keyword") String keyword);

}