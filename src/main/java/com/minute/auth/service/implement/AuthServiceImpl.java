package com.minute.auth.service.implement;

import com.minute.auth.common.CertificationNumber;
import com.minute.auth.common.CertificationStorage;
import com.minute.auth.dto.request.*;
import com.minute.auth.dto.response.*;
import com.minute.security.handler.EmailProvider;
import com.minute.security.handler.JwtProvider;
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    //의존성 주입
    private final JwtProvider jwtProvider;

    private final EmailProvider emailProvider;
    private final CertificationStorage certificationStorage;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 20)
            return false;
        // 영문/숫자/특수문자 조합 정규식
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/~`]).{8,20}$";
        return password.matches(pattern);
    }

    @Override
    public ResponseEntity<? super SignupValidateResponseDto> validateSignUp(SignupValidateRequestDto dto) {
        try {
            String userId = dto.getUserId();
            boolean existedId = userRepository.existsByUserId(userId);
            if (existedId) return SignupValidateResponseDto.duplicateId();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return SignupValidateResponseDto.success();
    }


    //회원가입
    @Override
    public ResponseEntity<? super SignupResponseDto> signUp(SignUpRequestDTO dto) {

        try{
            //중복정보 있는지 검사
            String id = dto.getUserId();
            boolean existedId = userRepository.existsByUserId(id);
            if (existedId) return SignupResponseDto.duplicateId();

            // 비밀번호 조건 검사 추가
            String password = dto.getUserPw();
            if (!isValidPassword(password)) {
                return SignupResponseDto.invalidPassword(); //
            }

            String email = dto.getUserEmail();
            boolean existedEmail = userRepository.existsByUserEmail(email);
            if (existedEmail) return SignupResponseDto.duplicateEmail();

            String nickname = dto.getUserNickName();
            boolean existedNickname = userRepository.existsByUserNickName(nickname);
            if (existedNickname) return SignupResponseDto.duplicateNickName();

            String phone = dto.getUserPhone();
            boolean existedPhone = userRepository.existsByUserPhone(phone);
            if (existedPhone) return SignupResponseDto.duplicatePhone();

            //비번 암호화
            String pw = dto.getUserPw();
            String encodedPassword = passwordEncoder.encode(pw);
            dto.setUserPw(encodedPassword);

            // 2. userNo 자동 할당
            Long maxUserNo = userRepository.findMaxUserNo();

            // 새 userNo는 최대값 + 1
            Long newUserNo = maxUserNo + 1;

            //db 저장
            User user = new User(dto);
            user.setUserNo(Math.toIntExact(newUserNo));
            userRepository.save(user);


        }catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return SignupResponseDto.success();
    }

    //인증번호 보내기
    @Override
    public ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto) {
        try {

            String userEmail = dto.getUserEmail();

            // 2. 인증번호 생성
            String certificationNumber = CertificationNumber.getCertificationNumber();

            // 3. 이메일 발송
            boolean isSuccessed = emailProvider.sendCertificationMail(userEmail, certificationNumber);
            if (!isSuccessed) return EmailCertificationResponseDto.mailSendFail();

            // 4. 인증번호 임시 저장
            certificationStorage.save(userEmail, certificationNumber);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }

        return EmailCertificationResponseDto.success();
    }


    //인증번호 검증하기
    @Override
    public ResponseEntity<?> verifyCertificationCodeForSignUp(VerifyCodeRequestDto dto) {
        String userEmail = dto.getUserEmail();
        String certificationNumber = dto.getCertificationNumber();

        // 1. 임시 저장소에서 인증번호 조회
        String storedNumber = certificationStorage.get(userEmail);
        if (storedNumber == null || !storedNumber.equals(certificationNumber)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        return ResponseEntity.ok().body("인증 성공");
    }


    //인증번호 검증하기
    @Override
    public ResponseEntity<?> verifyCertificationCode(VerifyCodeRequestDto dto) {
        String userEmail = dto.getUserEmail();
        String certificationNumber = dto.getCertificationNumber();

        // 1. 임시 저장소에서 인증번호 조회
        String storedNumber = certificationStorage.get(userEmail);
        if (storedNumber == null || !storedNumber.equals(certificationNumber)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        // 2. 사용자 조회
        User user = userRepository.findByUserEmail(userEmail);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("사용자를 찾을 수 없습니다.");
        }

        user.setCertified(true);
        userRepository.save(user);

        return ResponseEntity.ok().body("인증 성공");
    }

    //비번재설정
    @Transactional
    @Override
    public ResponseEntity<? super ResponseDto> resetPassword(ResetPasswordRequestDto dto) {
        try {
            String userEmail = dto.getUserEmail();
            String newPassword = dto.getNewPassword();

            System.out.println("userEmail: " + userEmail);
            System.out.println("newPassword: " + newPassword);

            // 1. 비밀번호 유효성 검사
            if (!isValidPassword(newPassword)) {
                return SignupResponseDto.invalidPassword();
            }

            // 2. 사용자 존재 여부 확인
            User user = userRepository.findByUserEmail(userEmail);
            if (user == null) {
                return ResetPasswordResponseDto.userNotFound();
            }

            // 3. 비밀번호 암호화 후 저장
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setUserPw(encodedPassword);
            userRepository.save(user);

            return ResetPasswordResponseDto.success();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
    }

}



