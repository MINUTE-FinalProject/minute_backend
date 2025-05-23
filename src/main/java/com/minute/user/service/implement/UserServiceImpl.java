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

//아이디로 유저찾기
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager em;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String userEmail) {

        User user;
        try {
            user = userRepository.findById(userEmail)
                    .orElse(null);

            if (user == null) return GetSignInUserResponseDto.notExistUser();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return GetSignInUserResponseDto.success(user);

    }

    @Override
    public ResponseEntity<? super GetUserResponseDto> getUser(String userId) {
        User user = null;
        try {
            user = userRepository.findUserByUserId(userId);
            if (user == null) return GetUserResponseDto.notExistUser();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        return GetUserResponseDto.success(user);
    }

    //사용자 정보 수정
    @Override
    public ResponseEntity<? super UserPatchInfoResponseDto> userPatchInfo(UserPatchInfoRequestDto dto, String userId) {
        try {
            User user = userRepository.findUserByUserId(userId);
            if (user == null) return UserPatchInfoResponseDto.noExistUser();

            // 닉네임
            String nickName = dto.getUserNickName();
            if (nickName != null && !nickName.equals(user.getUserNickName())) {
                if (userRepository.existsByUserNickName(nickName)) {
                    System.out.println("중복 닉네임");
                    return UserPatchInfoResponseDto.duplicateNickName();
                }
                user.setUserNickName(nickName);
            }

            // 전화번호
            String phone = dto.getUserPhone();
            if (phone != null && !phone.equals(user.getUserPhone())) {
                if (userRepository.existsByUserPhone(phone))
                    return UserPatchInfoResponseDto.duplicatePhone();
                user.setUserPhone(phone);
            }

            // 이메일
            String email = dto.getUserEmail();
            if (email != null && !email.equals(user.getUserEmail())) {
                if (userRepository.existsByUserEmail(email))
                    return UserPatchInfoResponseDto.duplicateEmail();
                user.setUserEmail(email);
            }

            // 성별
            if (dto.getUserGender() != null)
                user.setUserGender(dto.getUserGender());

            // 프로필 이미지
            if (dto.getUserProfileImage() != null)
                user.setProfileImage(dto.getUserProfileImage());

            userRepository.save(user);
            em.flush();
            User updatedUser = userRepository.findByUserEmail(user.getUserEmail());
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

}
