package com.minute.plan.entity;

import com.minute.plan.dto.request.PlanRequestDTO;
import com.minute.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@AllArgsConstructor
@Builder
@Table(name = "plan")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @CreationTimestamp  // 엔티티가 처음 persist() 될 때 한 번만 현재 시각 설정
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp    // 엔티티를 update() 할 떄마다 현재 시각으로 갱신
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updateFrom(PlanRequestDTO dto) {
        this.travelDate = dto.getTravelDate();
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.startTime = dto.getStartTime();
        this.endTime = dto.getEndTime();
    }

}
