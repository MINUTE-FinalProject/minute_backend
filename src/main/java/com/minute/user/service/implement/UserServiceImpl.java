package com.minute.user.service.implement;

import com.minute.auth.dto.response.ResponseDto;
import com.minute.user.dto.request.UserPatchInfoRequestDto;
import com.minute.user.dto.response.GetSignInUserResponseDto;
import com.minute.user.dto.response.GetUserResponseDto;
import com.minute.user.dto.response.UserPatchInfoResponseDto;
import com.minute.user.entity.User;
import com.minute.user.enumpackage.Role;
import com.minute.user.repository.UserRepository;
import com.minute.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//아이디로 유저찾기
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager em;
    private final UserRepository userRepository;

    //프론트용 사용자 조회
    @Override
    public ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findUserByUserId(userId);
            if (optionalUser.isEmpty()) return GetSignInUserResponseDto.notExistUser();

            User user = optionalUser.get();
            return GetSignInUserResponseDto.success(user);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
    }

    @Override
    public ResponseEntity<? super GetUserResponseDto> getUser(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findUserByUserId(userId);
            if (optionalUser.isEmpty()) return GetUserResponseDto.notExistUser();

            return GetUserResponseDto.success(optionalUser.get());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
    }


    //사용자 정보 수정
    @Override
    public ResponseEntity<? super UserPatchInfoResponseDto> userPatchInfo(UserPatchInfoRequestDto dto, String userId) {
        try {
            Optional<User> optionalUser = userRepository.findUserByUserId(userId);
            if (optionalUser.isEmpty()) return UserPatchInfoResponseDto.noExistUser();

            User user = optionalUser.get();

            // 닉네임
            String nickName = dto.getUserNickName();
            if (nickName != null && !nickName.equals(user.getUserNickName())) {
                if (userRepository.existsByUserNickName(nickName)) {
                    return UserPatchInfoResponseDto.duplicateNickName();
                }
                user.setUserNickName(nickName);
            }

            // 전화번호
            String phone = dto.getUserPhone();
            if (phone != null && !phone.equals(user.getUserPhone())) {
                if (userRepository.existsByUserPhone(phone)) return UserPatchInfoResponseDto.duplicatePhone();
                user.setUserPhone(phone);
            }

            // 이메일
            String email = dto.getUserEmail();
            if (email != null && !email.equals(user.getUserEmail())) {
                if (userRepository.existsByUserEmail(email)) return UserPatchInfoResponseDto.duplicateEmail();
                user.setUserEmail(email);
            }

            // 기타 정보
            if (dto.getUserGender() != null) user.setUserGender(dto.getUserGender());
            if (dto.getUserProfileImage() != null) user.setProfileImage(dto.getUserProfileImage());

            userRepository.save(user);
            em.flush();
            return UserPatchInfoResponseDto.success();

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
    }


    //관리자 승격
    @Transactional
    public void promoteUserToAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

    //spring security용 사용자 조회(optional 필요)
    @Override
    public Optional<User> getUserEntityByEmail(String email) {
        return userRepository.findById(email);
    }

    @Override
    public ResponseEntity<? super ResponseDto> deleteUser(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findUserByUserId(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(404).body(new ResponseDto("NOT_FOUND", "사용자를 찾을 수 없습니다."));
            }

            userRepository.delete(optionalUser.get());
            return ResponseEntity.ok(new ResponseDto("SU", "회원 탈퇴가 완료되었습니다."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ResponseDto("SE", "서버 오류가 발생했습니다."));
        }
    }




}
