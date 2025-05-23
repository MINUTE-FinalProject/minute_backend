package com.minute.auth.service;

import com.minute.auth.dto.request.auth.SignInRequestDto;
import com.minute.auth.dto.request.auth.SignUpRequestDTO;
import com.minute.auth.dto.request.auth.SignupValidateRequestDto;
import com.minute.auth.dto.response.auth.SignInResponseDto;
import com.minute.auth.dto.response.auth.SignupResponseDto;
import com.minute.auth.dto.response.auth.SignupValidateResponseDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<? super SignupValidateResponseDto> validateSignUp(SignupValidateRequestDto dto);

    ResponseEntity<? super SignupResponseDto> signUp(SignUpRequestDTO dto);

    ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto);
}
