package com.minute.user.dto.response;

import com.minute.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllUsersResponseDto {
    private String code;
    private String message;
    private List<User> userList;

    public static ResponseEntity<GetAllUsersResponseDto> success(List<User> users) {
        return ResponseEntity.ok(new GetAllUsersResponseDto("SU", "전체 회원 조회 성공", users));
    }

    public static ResponseEntity<GetAllUsersResponseDto> databaseError() {
        return ResponseEntity.internalServerError()
                .body(new GetAllUsersResponseDto("SE", "데이터베이스 오류", null));
    }
}
