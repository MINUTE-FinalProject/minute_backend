package com.minute.user.service;

import com.minute.auth.dto.response.ResponseDto;
import com.minute.user.dto.request.UserPatchInfoRequestDto;
import com.minute.user.dto.response.GetSignInUserResponseDto;
import com.minute.user.dto.response.GetUserResponseDto;
import com.minute.user.dto.response.UserPatchInfoResponseDto;
import com.minute.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {

    ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String userId);

    ResponseEntity<? super GetUserResponseDto> getUser(String userId);

    ResponseEntity<? super UserPatchInfoResponseDto> userPatchInfo(UserPatchInfoRequestDto dto, String userId);

    Optional<User> getUserEntityByEmail(String email);

    ResponseEntity<? super ResponseDto> deleteUser(String userId);

    ResponseEntity<? super ResponseDto> uploadProfileImage(String userId, MultipartFile file);


}
