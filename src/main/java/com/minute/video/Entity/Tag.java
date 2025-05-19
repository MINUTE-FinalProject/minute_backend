package com.minute.video.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private int tagId;

    @Column(length = 100,nullable = false)
    private String tagName;

    @OneToMany(mappedBy = "tag",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<VideoTag> videoTags = new ArrayList<>();
}
