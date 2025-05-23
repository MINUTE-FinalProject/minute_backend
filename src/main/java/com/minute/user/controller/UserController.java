package com.minute.user.controller;

import com.minute.auth.dto.response.ResponseDto;
import com.minute.user.dto.request.UserPatchInfoRequestDto;
import com.minute.user.dto.response.GetSignInUserResponseDto;
import com.minute.user.dto.response.GetUserResponseDto;
import com.minute.user.dto.response.UserPatchInfoResponseDto;
import com.minute.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<?super GetSignInUserResponseDto> getSignInUser(@AuthenticationPrincipal String userEmail){
        ResponseEntity<? super GetSignInUserResponseDto> response = userService.getSignInUser(userEmail);
        return response;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<? super GetUserResponseDto> getUser(
            @PathVariable("userId") String userId
    ){
        ResponseEntity<? super GetUserResponseDto> response = userService.getUser(userId);
        return response;
    }


    @PatchMapping("/modify")
    public ResponseEntity<? super UserPatchInfoResponseDto> userPatchInfo(
            @RequestBody @Valid UserPatchInfoRequestDto requestBody,
            BindingResult bindingResult,
            @AuthenticationPrincipal String userId) {

        if (bindingResult.hasErrors()) {
            // 가장 첫 번째 에러 메시지
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseDto("VALIDATION_FAILED", errorMessage));
        }
        return userService.userPatchInfo(requestBody, userId);
    }


}
