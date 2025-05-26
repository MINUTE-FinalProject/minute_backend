package com.minute.checklist.entity;

import com.minute.plan.entity.Plan;
import com.minute.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@AllArgsConstructor
@Builder
@Table(name = "checklist")
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id", nullable = false)
    private Integer checklistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "item_content", nullable = false)
    private String itemContent;

    @Column(name = "is_checked", nullable = false)
    private Boolean isChecked = false;  // 필드 객체 생성 시 기본 false

    @CreationTimestamp  // 엔티티가 처음 persist() 될 때 한 번만 현재 시각 설정
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp    // 엔티티를 update() 할 떄마다 현재 시각으로 갱신
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
