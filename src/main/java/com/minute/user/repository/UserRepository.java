package com.minute.user.repository;

import com.minute.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // @Query 어노테이션으로 최대 userNo 조회
    @Query("SELECT COALESCE(MAX(u.userNo), 0) FROM User u")
    Long findMaxUserNo();

    boolean existsByUserId(String userId);
    boolean existsByUserEmail(String userEmail);
    boolean existsByUserNickName(String userNickName);
    boolean existsByUserPhone(String userPhone);



    User findByUserEmail(String userEmail);

    Optional<User> findByUserNameAndUserEmailAndUserPhone(String userName, String userEmail, String userPhone);

    //    User findUserByUserId(String userId); 완수 수정했음
    Optional<User> findUserByUserId(String userId); // 반환 타입을 Optional<User>로 변경
//    UserRepository 인터페이스에서 findUserByUserId 메서드의 반환 타입을 Optional<User>로 변경하는 것입니다. 이렇게 하면 Spring Data JPA가 자동으로 처리해주며, 서비스 코드에서 .orElseThrow()를 자연스럽게 사용할 수 있습니다.

}