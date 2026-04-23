package com.silveira.care360.domain.model;

public class GroupUserInput {

    private final String userId;
    private final String email;
    private final String displayName;

    public GroupUserInput(String userId, String email, String displayName) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }
}
