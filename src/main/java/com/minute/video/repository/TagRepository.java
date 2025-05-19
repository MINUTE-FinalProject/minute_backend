package com.minute.video.repository;

import com.minute.video.Entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    // 태그 이름으로 조회
    Optional<Tag> findByName(String tagName);
}
