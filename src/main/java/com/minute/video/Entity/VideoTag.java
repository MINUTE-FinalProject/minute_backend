package com.minute.video.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "video_tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoTag {
    @EmbeddedId
    private VideoTagId id;

    // video_id 컬럼은 VideoTagId.videoId 와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("videoId")
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    // tag_id 컬럼은 VideoTagId.tagId 와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    // 편의를 위해 생성자 추가 (builder 대신 사용할 때 유용)
    public VideoTag(Video video, Tag tag) {
        this.video = video;
        this.tag = tag;
        this.id = new VideoTagId(video.getVideoId(), tag.getTagId());
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoTagId implements Serializable {

        @Column(length = 50)
        private String videoId;

        @Column
        private int tagId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VideoTagId that = (VideoTagId) o;
            return tagId == that.tagId &&
                    Objects.equals(videoId, that.videoId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(videoId, tagId);
        }
    }
}
