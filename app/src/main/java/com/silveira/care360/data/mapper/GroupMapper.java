package com.silveira.care360.data.mapper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.silveira.care360.data.local.entity.GroupEntity;
import com.silveira.care360.data.remote.dto.GroupDocumentDto;
import com.silveira.care360.domain.model.Group;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper para transformar modelos locales/remotos al dominio Group.
 */
public class GroupMapper {

    public static Group fromEntity(GroupEntity entity) {
        if (entity == null) return null;

        return new Group(
                entity.getId(),
                entity.getName(),
                entity.getCareName(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                entity.getJoinCode(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isActive()
        );
    }

    public static GroupEntity toEntity(Group group) {
        if (group == null) return null;

        return new GroupEntity(
                group.getId(),
                group.getName(),
                group.getCareName(),
                group.getJoinCode(),
                group.getCreatedBy(),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                group.isActive()
        );
    }

    public static Group fromDto(GroupDocumentDto dto) {
        if (dto == null) return null;

        return new Group(
                dto.getId() != null ? dto.getId() : "",
                dto.getName(),
                dto.getCareName(),
                dto.getCareAge(),
                dto.getCarePhotoUri(),
                dto.getCarePhotoUrl(),
                dto.getCarePhone(),
                dto.getCareAddress(),
                dto.getEmergencyContactName(),
                dto.getEmergencyContactPhone(),
                dto.getCareAllergies(),
                dto.getCareConditions(),
                dto.getJoinCode(),
                dto.getCreatedBy(),
                dto.getCreatedAt(),
                dto.getUpdatedAt(),
                dto.isActive()
        );
    }

    public static GroupDocumentDto fromFirestore(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        GroupDocumentDto dto = new GroupDocumentDto();
        dto.setId(document.getId());
        dto.setName(document.getString("name"));
        dto.setJoinCode(document.getString("joinCode"));
        dto.setCareName(document.getString("careName"));
        Object careAgeObj = document.get("careAge");
        if (careAgeObj instanceof Number) {
            dto.setCareAge(((Number) careAgeObj).intValue());
        }
        dto.setCarePhotoUri(document.getString("carePhotoUri"));
        dto.setCarePhotoUrl(document.getString("carePhotoUrl"));
        dto.setCarePhone(document.getString("carePhone"));
        dto.setCareAddress(document.getString("careAddress"));
        dto.setEmergencyContactName(document.getString("emergencyContactName"));
        dto.setEmergencyContactPhone(document.getString("emergencyContactPhone"));
        dto.setCareAllergies(document.getString("careAllergies"));
        dto.setCareConditions(document.getString("careConditions"));
        dto.setCreatedBy(document.getString("createdBy"));

        Object createdAtObj = document.get("createdAt");
        if (createdAtObj instanceof com.google.firebase.Timestamp) {
            dto.setCreatedAt(((com.google.firebase.Timestamp) createdAtObj).toDate().getTime());
        } else if (createdAtObj instanceof Long) {
            dto.setCreatedAt((Long) createdAtObj);
        }

        Object updatedAtObj = document.get("updatedAt");
        if (updatedAtObj instanceof com.google.firebase.Timestamp) {
            dto.setUpdatedAt(((com.google.firebase.Timestamp) updatedAtObj).toDate().getTime());
        } else if (updatedAtObj instanceof Long) {
            dto.setUpdatedAt((Long) updatedAtObj);
        }

        Boolean active = document.getBoolean("active");
        dto.setActive(active != null && active);

        return dto;
    }

    public static Map<String, Object> toFirestoreMap(GroupDocumentDto dto, Object createdAtValue, Object updatedAtValue) {
        Map<String, Object> data = new HashMap<>();
        if (dto == null) {
            return data;
        }

        data.put("name", dto.getName());
        data.put("careName", dto.getCareName());
        data.put("careAge", dto.getCareAge());
        data.put("carePhotoUri", dto.getCarePhotoUri());
        data.put("carePhotoUrl", dto.getCarePhotoUrl());
        data.put("carePhone", dto.getCarePhone());
        data.put("careAddress", dto.getCareAddress());
        data.put("emergencyContactName", dto.getEmergencyContactName());
        data.put("emergencyContactPhone", dto.getEmergencyContactPhone());
        data.put("careAllergies", dto.getCareAllergies());
        data.put("careConditions", dto.getCareConditions());
        data.put("joinCode", dto.getJoinCode());
        data.put("createdBy", dto.getCreatedBy());
        data.put("createdAt", createdAtValue);
        data.put("updatedAt", updatedAtValue);
        data.put("active", dto.isActive());
        return data;
    }
}
