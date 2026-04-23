package com.silveira.care360.data.remote.dto;

public class GroupDocumentDto {

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
