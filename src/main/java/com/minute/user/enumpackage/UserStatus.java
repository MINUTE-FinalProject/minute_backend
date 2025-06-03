package com.minute.user.enumpackage;

import lombok.Getter;

@Getter
public enum UserStatus {
    N,Y;

    public UserStatus toggle() {
        return this == Y ? N : Y;
    }
}
