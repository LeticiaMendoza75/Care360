package com.silveira.care360.domain.model;

import androidx.annotation.NonNull;

public class Group {

    @NonNull
    private String id;
    private String name;
    private String careName;
    private Integer careAge;
    private String carePhotoUri;
    private String carePhotoUrl;
    private String carePhone;
    private String careAddress;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String careAllergies;
    private String careConditions;
    private String joinCode;
    private String createdBy;
    private long createdAt;
    private long updatedAt;
    private boolean active;

    public Group() {
        this.id = "";
    }

    public Group(@NonNull String id, String name, String careName, String joinCode,
                 String createdBy, long createdAt, long updatedAt, boolean active) {
        this(id, name, careName, null, null, null, null, null, null, null, joinCode, createdBy, createdAt, updatedAt, active);
    }

    public Group(@NonNull String id, String name, String careName, Integer careAge, String carePhotoUri,
                 String joinCode, String createdBy, long createdAt, long updatedAt, boolean active) {
        this(id, name, careName, careAge, carePhotoUri, null, null, null, null, null, joinCode, createdBy, createdAt, updatedAt, active);
    }

    public Group(@NonNull String id, String name, String careName, Integer careAge, String carePhotoUri,
                 String carePhotoUrl, String joinCode, String createdBy, long createdAt, long updatedAt, boolean active) {
        this(id, name, careName, careAge, carePhotoUri, carePhotoUrl, null, null, null, null, joinCode, createdBy, createdAt, updatedAt, active);
    }

    public Group(@NonNull String id, String name, String careName, Integer careAge, String carePhotoUri,
                 String carePhotoUrl, String carePhone, String careAddress,
                 String emergencyContactName, String emergencyContactPhone,
                 String joinCode, String createdBy, long createdAt, long updatedAt, boolean active) {
        this(id, name, careName, careAge, carePhotoUri, carePhotoUrl, carePhone, careAddress,
                emergencyContactName, emergencyContactPhone, "", "", joinCode, createdBy, createdAt, updatedAt, active);
    }

    public Group(@NonNull String id, String name, String careName, Integer careAge, String carePhotoUri,
                 String carePhotoUrl, String carePhone, String careAddress,
                 String emergencyContactName, String emergencyContactPhone,
                 String careAllergies, String careConditions,
                 String joinCode, String createdBy, long createdAt, long updatedAt, boolean active) {
        this.id = id;
        this.name = name;
        this.careName = careName;
        this.careAge = careAge;
        this.carePhotoUri = carePhotoUri;
        this.carePhotoUrl = carePhotoUrl;
        this.carePhone = carePhone;
        this.careAddress = careAddress;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
        this.careAllergies = careAllergies;
        this.careConditions = careConditions;
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

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCareName() {
        return careName;
    }

    public void setCareName(String careName) {
        this.careName = careName;
    }

    public Integer getCareAge() {
        return careAge;
    }

    public void setCareAge(Integer careAge) {
        this.careAge = careAge;
    }

    public String getCarePhotoUri() {
        return carePhotoUri;
    }

    public void setCarePhotoUri(String carePhotoUri) {
        this.carePhotoUri = carePhotoUri;
    }

    public String getCarePhotoUrl() {
        return carePhotoUrl;
    }

    public void setCarePhotoUrl(String carePhotoUrl) {
        this.carePhotoUrl = carePhotoUrl;
    }

    public String getCarePhone() {
        return carePhone;
    }

    public void setCarePhone(String carePhone) {
        this.carePhone = carePhone;
    }

    public String getCareAddress() {
        return careAddress;
    }

    public void setCareAddress(String careAddress) {
        this.careAddress = careAddress;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getCareAllergies() {
        return careAllergies;
    }

    public void setCareAllergies(String careAllergies) {
        this.careAllergies = careAllergies;
    }

    public String getCareConditions() {
        return careConditions;
    }

    public void setCareConditions(String careConditions) {
        this.careConditions = careConditions;
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
