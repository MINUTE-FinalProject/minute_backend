package com.minute.user.controller;

import com.minute.user.dto.request.auth.SignInRequestDto;
import com.minute.user.dto.request.auth.SignUpRequestDTO;
import com.minute.user.dto.request.auth.SignupValidateRequestDto;
import com.minute.user.dto.response.ResponseDto;
import com.minute.user.dto.response.auth.SignInResponseDto;
import com.minute.user.dto.response.auth.SignupResponseDto;
import com.minute.user.dto.response.auth.SignupValidateResponseDto;
import com.minute.user.repository.UserRepository;
import com.minute.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/sign-up/validate")
    public ResponseEntity<? super SignupValidateResponseDto> validateSignUp(@RequestBody SignupValidateRequestDto dto) {
        return authService.validateSignUp(dto);
    }



    @PostMapping("/sign-up")
    public ResponseEntity<? super SignupResponseDto> signUp(@RequestBody @Valid SignUpRequestDTO requestBody) {
        ResponseEntity<? super SignupResponseDto> response = authService.signUp(requestBody);
        return response;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<? super SignInResponseDto> signIn(@RequestBody @Valid SignInRequestDto requestBody) {
        ResponseEntity<? super SignInResponseDto> response = authService.signIn(requestBody);
        return response;
    }
}
