package com.minute.auth.service;

import com.minute.auth.dto.request.*;
import com.minute.auth.dto.response.EmailCertificationResponseDto;
import com.minute.auth.dto.response.ResetPasswordResponseDto;
import com.minute.auth.dto.response.SignupResponseDto;
import com.minute.auth.dto.response.SignupValidateResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

public interface AuthService {

    ResponseEntity<? super SignupValidateResponseDto> validateSignUp(SignupValidateRequestDto dto);

    ResponseEntity<? super SignupResponseDto> signUp(SignUpRequestDTO dto);

    ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto);

    ResponseEntity<?> verifyCertificationCode(VerifyCodeRequestDto dto);

    ResponseEntity<?> verifyCertificationCodeForSignUp(VerifyCodeRequestDto dto);

    ResponseEntity<? super ResetPasswordResponseDto> resetPassword(ResetPasswordRequestDto dto);
}
