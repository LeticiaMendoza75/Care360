package com.silveira.care360.core.auth;

public class GoogleAuthResult {

    private final String idToken;
    private final String email;
    private final String displayName;
    private final String photoUrl;

    public GoogleAuthResult(String idToken, String email, String displayName, String photoUrl) {
        this.idToken = idToken;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
