package com.minute.user.controller;

import com.minute.auth.dto.response.ResponseDto;
import com.minute.auth.service.DetailUser;
import com.minute.user.dto.request.UserPatchInfoRequestDto;
import com.minute.user.dto.response.GetAllUsersResponseDto;
import com.minute.user.dto.response.GetSignInUserResponseDto;
import com.minute.user.dto.response.GetUserResponseDto;
import com.minute.user.dto.response.UserPatchInfoResponseDto;
import com.minute.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(@AuthenticationPrincipal DetailUser detailUser) {
        if (detailUser == null || detailUser.getUser() == null) {
            // 인증 정보 없으면 401 Unauthorized 처리
            return ResponseEntity.status(401).build();
        }

        String userId = detailUser.getUser().getUserId();

        if (userId == null) {
            // userId가 null이면 에러 로그 찍고 401 또는 400 반환
            System.out.println("로그인 사용자 userId가 null입니다!");
            return ResponseEntity.status(400).body("사용자 ID가 존재하지 않습니다.");
        }
        return userService.getSignInUser(userId);
    }


    // 특정 유저 조회
    @GetMapping("/{userId}")
    public ResponseEntity<? super GetUserResponseDto> getUser(
            @PathVariable("userId") String userId
    ) {
        return userService.getUser(userId);
    }

    // 유저 정보 수정
    @PatchMapping("/modify")
    public ResponseEntity<? super UserPatchInfoResponseDto> userPatchInfo(
            @RequestBody @Valid UserPatchInfoRequestDto requestBody,
            BindingResult bindingResult,
            @AuthenticationPrincipal DetailUser detailUser) {

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseDto("VALIDATION_FAILED", errorMessage));
        }

        if (detailUser == null) {
            return ResponseEntity.status(401).build();
        }
        // 수정 권한 판단 시 사용되는 유저 ID
        String userId = detailUser.getUser().getUserId();

        return userService.userPatchInfo(requestBody, userId);
    }

    //회원탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<? super ResponseDto> deleteUser(
            @AuthenticationPrincipal DetailUser detailUser) {

        if (detailUser == null || detailUser.getUser() == null) {
            return ResponseEntity.status(401).body(new ResponseDto("UNAUTHORIZED", "인증되지 않은 사용자입니다."));
        }

        String userId = detailUser.getUser().getUserId();

        return userService.deleteUser(userId);
    }

    //프로필 업로드
    @PostMapping("/profile")
    public ResponseEntity<? super ResponseDto> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal DetailUser detailUser
    ) {
        if (detailUser == null || detailUser.getUser() == null) {
            return ResponseEntity.status(401).body(new ResponseDto("UNAUTHORIZED", "인증되지 않은 사용자입니다."));
        }

        String userId = detailUser.getUser().getUserId();
        return userService.uploadProfileImage(userId, file);
    }

    //전체 유저 조회
    @GetMapping("/all")
    public ResponseEntity<? super GetAllUsersResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }

}
