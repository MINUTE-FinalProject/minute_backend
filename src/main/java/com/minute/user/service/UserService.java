package com.minute.user.service;

import com.minute.user.dto.request.UserPatchInfoRequestDto;
import com.minute.user.dto.response.GetSignInUserResponseDto;
import com.minute.user.dto.response.GetUserResponseDto;
import com.minute.user.dto.response.UserPatchInfoResponseDto;
import org.springframework.http.ResponseEntity;

public interface UserService {

    ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String userEmail);

    ResponseEntity<? super GetUserResponseDto> getUser(String userId);

    ResponseEntity<? super UserPatchInfoResponseDto> userPatchInfo(UserPatchInfoRequestDto dto,String userId);



}
