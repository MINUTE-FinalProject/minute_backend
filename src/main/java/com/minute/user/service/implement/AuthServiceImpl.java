package com.minute.user.service.implement;

import com.minute.security.handler.JwtProvider;
import com.minute.user.dto.request.auth.SignInRequestDto;
import com.minute.user.dto.request.auth.SignUpRequestDTO;
import com.minute.user.dto.response.ResponseDto;
import com.minute.user.dto.response.auth.SignInResponseDto;
import com.minute.user.dto.response.auth.SignupResponseDto;
import com.minute.user.entity.User;
import com.minute.user.repository.UserRepository;
import com.minute.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    //의존성 주입
    private final JwtProvider jwtProvider;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public ResponseEntity<? super SignupResponseDto> signUp(SignUpRequestDTO dto) {

        try{
            //중복정보 있는지 검사
            String id = dto.getUserId();
            boolean existedId = userRepository.existsByUserId(id);
            if (existedId) return SignupResponseDto.duplicateId();

            String email = dto.getUserEmail();
            boolean existedEmail = userRepository.existsByUserEmail(email);
            if (existedEmail) return SignupResponseDto.duplicateEmail();

            String nickname = dto.getUserNickname();
            boolean existedNickname = userRepository.existsByUserNickname(nickname);
            if (existedNickname) return SignupResponseDto.duplicateNickname();

            String phone = dto.getUserPhone();
            boolean existedPhone = userRepository.existsByUserPhone(phone);
            if (existedPhone) return SignupResponseDto.duplicatePhone();

            //비번 암호화
            String password = dto.getUserPw();
            String encodedPassword = passwordEncoder.encode(password);
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

    @Override
    public ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto) {

        String token = null;

        try {

            String id = dto.getUserId();
            User user = userRepository.findUserByUserId(id);
            if(user == null) {System.out.println("로그인 실패 - 유저 없음");return SignInResponseDto.signInFailed();}

            String password = dto.getUserPw();
            String encodedPassword = user.getUserPw();
            boolean isMatched = passwordEncoder.matches(password, encodedPassword);
            if(!isMatched) {System.out.println("로그인 실패- 비밀번호 불일치");return SignInResponseDto.wrongPw();}

            token = jwtProvider.create(id);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return SignInResponseDto.success(token);
    }
}
