package com.minute.video.repository;

import com.minute.user.entity.User;
import com.minute.video.Entity.Video;
import com.minute.video.Entity.VideoLikes;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoLikesRepository extends JpaRepository<VideoLikes, Integer> {

    // 특정 사용자가 좋아요한 영상 목록
    @Query("SELECT vl FROM VideoLikes vl JOIN FETCH vl.video WHERE vl.user.userId = :userId")
    List<VideoLikes> findByUserUserId(@Param("userId") String userId);

    // 기존 findByUserUserId(String userId)는 VideoLikes만 가져오고, 그 안의 video는 지연 로딩(Lazy Loading) 상태라서 null이거나 프록시 객체였기 때문에,
    //→ like.getVideo().getVideoId()가 null로 나왔던 겁니다.

    // 영상 좋아요 여부 확인
    boolean existsByUserUserIdAndVideoVideoId(String userId, String videoId);

    // 영상 좋아요 개수 조회
    Long countByVideoVideoId(String videoId);

    // 삭제한 행 수를 반환하도록 변경
    @Modifying
    @Transactional
    int deleteByUserUserIdAndVideoVideoId(String userId, String videoId);

}
