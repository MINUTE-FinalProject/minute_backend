package com.minute.user.entity;

import com.minute.user.enumpackage.Gender;
import com.minute.user.enumpackage.Role;
import com.minute.user.enumpackage.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_pw", nullable = false)
    private String userPw;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "user_nickname", nullable = false)
    private String userNickname;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role userRole = Role.User;

    @Column(name = "created_at", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date userCreatedAt;

    @Column(name = "updated_at", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date userUpdatedAt;

    @Column(name = "user_phone",nullable = false)
    private String userPhone;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_status",nullable = false)
    @Enumerated(EnumType.STRING)
    private Status userStatus = Status.N;

    @Column(name = "user_gender",nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender userGender = Gender.Male;

    @Column(name = "user_no", nullable = false, updatable = false, insertable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userNo;

    @Column(name = "user_report")
    private Integer userReport;




}
