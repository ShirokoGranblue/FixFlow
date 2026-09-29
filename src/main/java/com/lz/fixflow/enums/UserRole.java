package com.lz.fixflow.enums;

import lombok.Getter;

@Getter
public enum UserRole {

    USER("USER"),
    ADMIN("ADMIN"),
    STAFF("STAFF");

    private final String value;
    UserRole(String value) {
        this.value = value;
    }
}
