package com.minute.folder.entity;

import com.minute.bookmark.entity.Bookmark;
import com.minute.user.entity.User; // User 엔티티 import
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // createdAt 자동 생성을 위해 추가

import java.time.LocalDateTime;
import java.util.ArrayList; // bookmarks 초기화를 위해 추가
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "folder")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Integer folderId;

    @Column(name = "folder_name", nullable = false, length = 10)
    private String folderName;

    @CreationTimestamp // 엔티티 생성 시 자동으로 현재 시간 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Bookmark> bookmarks = new ArrayList<>();

}