package com.minute.video.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "popular_search")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularSearch {

    @Id
    @Column(length = 100)
    private String keyword;

    @Column(nullable = false)
    private int searchCount;

    @Column(nullable = false,columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
