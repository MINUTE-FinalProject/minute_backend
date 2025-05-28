package com.minute.video.repository;

import com.minute.video.Entity.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface YoutubeVideoRepository extends JpaRepository<YoutubeVideo, String> {
    // 예시: 지역/도시별 영상 찾기
    List<YoutubeVideo> findByRegion(String region);

    List<YoutubeVideo> findByRegionAndCity(String region, String city);
}
