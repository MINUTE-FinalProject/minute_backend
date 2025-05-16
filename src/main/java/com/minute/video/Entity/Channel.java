package com.minute.video.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "channel")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {

    @Id
    @Column(length = 100)
    private String channelId;

    @Column(length = 255, nullable = false)
    private String channelName;

    @Column(columnDefinition = "TEXT")
    private String channelDescription;

    @Column(length = 255)
    private String channelThumbnail;
}
