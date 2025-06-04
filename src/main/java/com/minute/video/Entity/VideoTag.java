package com.minute.video.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ManyToOne
    @MapsId("videoId")
    @JoinColumn(name = "video_id")
    @JsonIgnore // 영상-태그의 반대편도 무한루프 방지!
    private Video video;

    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    @JsonIgnore // 태그 → 비디오 연결도 무한참조 방지
    private Tag tag;


    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoTagId implements Serializable {
        private String videoId;
        private int tagId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VideoTagId that = (VideoTagId) o;
            return Objects.equals(videoId, that.videoId) &&
                    Objects.equals(tagId, that.tagId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(videoId, tagId);
        }
    }
}
