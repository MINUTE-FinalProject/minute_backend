package com.minute.video.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    private String videoId;

    @Column(length = 255, nullable = false)
    private String videoTitle;

    @Column(columnDefinition = "TEXT")
    private String videoDescription;

    @Column(length = 255, nullable = false)
    private String videoUrl;

    @Column(length = 255)
    private String thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private Channel channel;
}
