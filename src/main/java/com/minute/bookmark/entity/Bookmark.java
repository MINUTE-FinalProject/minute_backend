package com.minute.bookmark.entity;

import com.minute.folder.entity.Folder;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bookmark", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "video_id", "folder_id"})
})
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookmarkId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "video_id", nullable = false)
    private String videoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;
}