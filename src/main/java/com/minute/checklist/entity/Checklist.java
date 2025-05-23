package com.minute.checklist.entity;

import com.minute.plan.entity.Plan;
import com.minute.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "checklist")
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    private Integer checklistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(name = "travel_date")
    private LocalDate travelDate;

    @Column(name = "item_content")
    private String itemContent;

    @Column(name = "is_checked")
    private Boolean isChecked = false;  // 필드 객체 생성 시 기본 false

    @CreationTimestamp  // 엔티티가 처음 persist() 될 때 한 번만 현재 시각 설정
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp    // 엔티티를 update() 할 떄마다 현재 시각으로 갱신
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
