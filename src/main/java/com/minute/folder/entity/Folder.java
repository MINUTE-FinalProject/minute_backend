package com.minute.folder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    private Integer folderId;

    @Column(name = "folder_name", nullable = false)
    private String folderName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}