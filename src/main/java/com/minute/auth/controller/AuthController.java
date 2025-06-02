package com.minute.auth.controller;

import com.minute.auth.dto.request.*;
import com.minute.auth.dto.response.EmailCertificationResponseDto;
import com.minute.auth.dto.response.ResponseDto;
import com.minute.auth.dto.response.SignupResponseDto;
import com.minute.auth.dto.response.SignupValidateResponseDto;
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.auth.service.AuthService;
import com.minute.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/sign-up/validate")
    public ResponseEntity<? super SignupValidateResponseDto> validateSignUp(@RequestBody SignupValidateRequestDto dto) {
        return authService.validateSignUp(dto);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<? super SignupResponseDto> signUp(@RequestBody @Valid SignUpRequestDTO requestBody) {
        ResponseEntity<? super SignupResponseDto> response = authService.signUp(requestBody);
        return response;
    }

    @PostMapping("/find-pw")
    public ResponseEntity<? super EmailCertificationResponseDto> emailCertification (@RequestBody @Valid EmailCertificationRequestDto requestBody) {
        ResponseEntity<? super EmailCertificationResponseDto> response = authService.emailCertification(requestBody);
        return response;
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequestDto dto) {
        return authService.verifyCertificationCode(dto);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDto dto) {
        return authService.resetPassword(dto);
    }

    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        boolean exists = userRepository.existsByUserEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/find-id")
    public ResponseEntity<?> findUserId(@RequestBody Map<String, String> request) {
        String userName = request.get("name");
        String userEmail = request.get("email");
        String userPhone = request.get("phone");

        Optional<User> userOptional = userRepository.findByUserNameAndUserEmailAndUserPhone(userName, userEmail, userPhone);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(Map.of("userId", userOptional.get().getUserId()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }
    }



}
