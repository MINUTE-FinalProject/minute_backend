package com.minute.video.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "video_category")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoCategory {

    // 복합키 처리
    @EmbeddedId
    private VideoCategoryId id;

    @ManyToOne
    @MapsId("videoId")
    @JoinColumn(name = "video_id")
    private Video video;


    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;

    // 복합키 처리하기 위해 사용되는 임베디드 키 클래스
    // 엔티티의 기본 키(pk)가 여러 컬럼으로 구성된 경우, 하나의 객체로 묶어 관리한다.
    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoCategoryId implements Serializable {
        private String videoId;
        private int categoryId;
    }
}
