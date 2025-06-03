package com.minute.video.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.minute.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Column(length = 255)
    private String region;

    @Column(length = 255)
    private String city;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<VideoCategory> videoCategories;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<VideoTag> videoTags;

    // 추천 로직에 필요한 속성
    private long views;
    private long likes;

    // 좋아요 증가
    public void increaseLikes() {
        this.likes += 1;
    }

    // 좋아요 감소
    public void decreaseLikes() {
        this.likes = Math.max(0, this.likes - 1);   // -1이 안되게 최소 0
    }
}