package com.minute.video.repository;

import com.minute.video.Entity.VideoTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoTagRepository extends JpaRepository<VideoTag, Integer> {
}