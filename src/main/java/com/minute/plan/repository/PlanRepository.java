package com.minute.plan.repository;

import com.minute.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {

    @Query("SELECT DISTINCT p.travelDate FROM Plan p WHERE p.user.userId = :userId AND p.travelDate BETWEEN :startDate AND :endDate")
    List<LocalDate> findTravelDatesInMonth(@Param("userId") String userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    List<Plan> findAllByUser_UserIdAndTravelDate(String userId, LocalDate travelDate);

}
