package com.minute.video.repository;

import com.minute.video.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // 카테고리 이름으로 조회
    Optional<Category> findByCategoryName(String categoryName);

    // 모든 카테고리 조회
    List<Category> findAll();
}
