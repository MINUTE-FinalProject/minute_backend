package com.minute.user.repository;

import com.minute.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // @Query 어노테이션으로 최대 userNo 조회
    @Query("SELECT COALESCE(MAX(u.userNo), 0) FROM User u")
    Long findMaxUserNo();

    boolean existsByUserId(String userId);
    boolean existsByUserEmail(String userEmail);
    boolean existsByUserNickName(String userNickName);
    boolean existsByUserPhone(String userPhone);

    User findUserByUserId(String userId);
    User findByUserEmail(String userEmail);
}


