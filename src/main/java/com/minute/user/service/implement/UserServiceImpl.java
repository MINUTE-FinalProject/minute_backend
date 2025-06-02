package com.minute.user.service.implement;

import com.minute.auth.dto.response.ResponseDto;
import com.minute.user.dto.request.UserPatchInfoRequestDto;
import com.minute.user.dto.response.GetAllUsersResponseDto;
import com.minute.user.dto.response.GetSignInUserResponseDto;
import com.minute.user.dto.response.GetUserResponseDto;
import com.minute.user.dto.response.UserPatchInfoResponseDto;
import com.minute.user.entity.User;
import com.minute.user.enumpackage.Role;
import com.minute.user.enumpackage.UserStatus;
import com.minute.user.repository.UserRepository;
import com.minute.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.minute.user.enumpackage.UserStatus.N;

//아이디로 유저찾기
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager em;
    private final UserRepository userRepository;
    String uploadDir = "C:/upload/profile/";


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

    @Override
    public Optional<User> getUserEntityByEmail(String email) {
        return userRepository.findById(email);
    }

    //회원탈퇴
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

    //프로필 업로드
    @Override
    public ResponseEntity<? super ResponseDto> uploadProfileImage(String userId, MultipartFile file) {
        try {
            Optional<User> optionalUser = userRepository.findUserByUserId(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(404).body(new ResponseDto("NOT_FOUND", "사용자를 찾을 수 없습니다."));
            }

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ResponseDto("INVALID_FILE", "이미지 파일이 비어 있습니다."));
            }

            // 파일 저장
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = userId + "_profile" + fileExtension;
            Path savePath = Paths.get(uploadDir, newFileName);
            Files.createDirectories(savePath.getParent());
            Files.write(savePath, file.getBytes());

            // DB에 경로 저장
            User user = optionalUser.get(); // ✅ 여기서 객체 꺼냄
            user.setProfileImage("/upload/" + newFileName);
            userRepository.save(user);

            return ResponseEntity.ok(new ResponseDto("SU", "프로필 이미지가 성공적으로 업로드되었습니다.", newFileName));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ResponseDto("FILE_ERROR", "파일 업로드 중 오류가 발생했습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
    }


    //회원 정지
    public void changeStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        user.setUserStatus(user.getUserStatus().toggle());

        userRepository.save(user);
    }

    //전체 유저 조회
    @Override
    public ResponseEntity<? super GetAllUsersResponseDto> getAllUsers() {
        try {
            List<User> userList = userRepository.findAll();
            return ResponseEntity.ok(GetAllUsersResponseDto.success(userList));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(GetAllUsersResponseDto.databaseError());
        }
    }


}
