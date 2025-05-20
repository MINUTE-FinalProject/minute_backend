package com.minute.user.service;

import com.minute.user.dto.request.auth.SignInRequestDto;
import com.minute.user.dto.request.auth.SignUpRequestDTO;
import com.minute.user.dto.response.auth.SignInResponseDto;
import com.minute.user.dto.response.auth.SignupResponseDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<? super SignupResponseDto> signUp(SignUpRequestDTO dto);

    ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto);
}
