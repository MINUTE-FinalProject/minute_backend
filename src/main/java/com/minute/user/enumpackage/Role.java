package com.minute.user.enumpackage;

import lombok.Getter;

@Getter
public enum Role {
    USER("USER"), ADMIN("ADMIN");
    private String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
