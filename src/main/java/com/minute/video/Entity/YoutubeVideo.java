package com.minute.video.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "youtube_videos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class YoutubeVideo {
    @Id
    @Column(name = "youtubevideo_id")
    private String youtubeVideoId;

    private String title;
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    private String region;
    private String city;
}