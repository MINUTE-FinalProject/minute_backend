package com.minute.user.controller;

import com.minute.user.service.UserService;
import com.minute.user.service.implement.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserServiceImpl userServiceImpl;

    @PostMapping("/promote/{userId}")
    public ResponseEntity<?> promoteToAdmin(@PathVariable String userId) {
        userServiceImpl.promoteUserToAdmin(userId);
        return ResponseEntity.ok("User promoted to ADMIN");
    }

    @PatchMapping("/status/{userId}")
    public ResponseEntity<?> changeStatus(@PathVariable String userId) {
        userServiceImpl.changeStatus(userId);
        return ResponseEntity.ok("회원 상태 변경 완료.");
    }

}
