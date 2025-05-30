package com.minute.user.controller;

import com.minute.auth.dto.response.ResponseDto;
import com.minute.user.service.UserService;
import com.minute.user.service.implement.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserServiceImpl userServiceImpl;
    private final UserService userService;

    @PatchMapping("/promote/{userId}")
    public ResponseEntity<?> promoteUserToAdmin(@PathVariable String userId) {
        userServiceImpl.promoteUserToAdmin(userId);
        return ResponseEntity.ok("관리자 승격 완료");
    }

    @PatchMapping("/status/{userId}")
    public ResponseEntity<?> changeStatus(@PathVariable String userId) {
        userServiceImpl.changeStatus(userId);
        return ResponseEntity.ok("회원 상태 변경 완료.");
    }

    //회원 삭제
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<? super ResponseDto> deleteUserByAdmin(@PathVariable String userId) {
        return userService.deleteUser(userId);
    }


}
