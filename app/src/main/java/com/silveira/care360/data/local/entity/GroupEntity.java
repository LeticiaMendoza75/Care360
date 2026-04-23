package com.silveira.care360.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class GroupEntity {

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String careName;
    private String joinCode;
    private String createdBy;
    private long createdAt;
    private long updatedAt;
    private boolean active;

    public GroupEntity() {
        this.id = "";
    }

    @Ignore
    public GroupEntity(@NonNull String id, String name, String careName, String joinCode,
                       String createdBy, long createdAt, long updatedAt, boolean active) {
        this.id = id;
        this.name = name;
        this.careName = careName;
        this.joinCode = joinCode;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.active = active;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCareName() {
        return careName;
    }

    public void setCareName(String careName) {
        this.careName = careName;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
}
