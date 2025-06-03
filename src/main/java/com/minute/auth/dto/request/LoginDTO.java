package com.minute.auth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginDTO {

    private String userId;
    private String userPw;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPw() {
        return userPw;
    }

    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    @Override
    public String toString() {
        return "LoginDto{" +
                "id='" + userId + '\'' +
                ", password='" + userPw + '\'' +
                '}';
    }
}
