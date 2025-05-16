package com.minute.video.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
    private Video video;

    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoTagId implements Serializable {
        private String videoId;
        private int tagId;
    }
}
