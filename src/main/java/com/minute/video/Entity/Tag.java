package com.minute.video.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tagId;

    @Column(length = 100, nullable = false, unique = true)
    private String tagName;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VideoTag> videoTags = new ArrayList<>();

    /**
     * ID 기반 equals/hashCode 정의
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag other = (Tag) o;
        // tagId가 0이 아니고, 같다면 동일 엔티티로 간주
        return this.tagId != 0 && this.tagId == other.tagId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId);
    }
}
