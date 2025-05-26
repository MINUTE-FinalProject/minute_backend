package com.minute.checklist.repository;

import com.minute.checklist.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Integer> {

    @Query("SELECT DISTINCT c.travelDate FROM Checklist c WHERE c.user.userId = :userId AND c.travelDate BETWEEN :startDate AND :endDate")
    List<LocalDate> findTravelDatesInMonth(@Param("userId") String userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    List<Checklist> findAllByUser_UserIdAndTravelDate(String userId, LocalDate travelDate);
}
