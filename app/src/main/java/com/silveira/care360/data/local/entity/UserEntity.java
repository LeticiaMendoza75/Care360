package com.silveira.care360.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    private String id;
    private String email;
    private String displayName;
    private String photoUrl;
    private String activeGroupId;
    private long createdAt;
    private long updatedAt;
    private boolean active;
    private String authProvider;

    public UserEntity() {
        this.id = "";
    }

    @Ignore
    public UserEntity(@NonNull String id, String email, String displayName, String photoUrl,
                      String activeGroupId, long createdAt, long updatedAt, boolean active,
                      String authProvider) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.activeGroupId = activeGroupId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
        this.authProvider = authProvider;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getActiveGroupId() {
        return activeGroupId;
    }

    public void setActiveGroupId(String activeGroupId) {
        this.activeGroupId = activeGroupId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }
}
