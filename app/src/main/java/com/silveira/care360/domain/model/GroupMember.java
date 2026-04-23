package com.silveira.care360.domain.model;

import androidx.annotation.NonNull;

public class GroupMember {

    @NonNull
    private String id;
    private String groupId;
    private String userId;
    private String role;
    private long joinedAt;
    private String invitedBy;
    private String status;
    private String groupName;
    private String careName;
    private String name;
    private String email;

    public GroupMember() {
        this.id = "";
    }

    public GroupMember(@NonNull String id, String groupId, String userId, String role,
                       long joinedAt, String invitedBy, String status) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.invitedBy = invitedBy;
        this.status = status;
    }

    public GroupMember(@NonNull String id, String groupId, String userId, String role,
                       long joinedAt, String invitedBy, String status,
                       String groupName, String careName) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.invitedBy = invitedBy;
        this.status = status;
        this.groupName = groupName;
        this.careName = careName;

    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCareName() {
        return careName;
    }

    public void setCareName(String careName) {
        this.careName = careName;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
