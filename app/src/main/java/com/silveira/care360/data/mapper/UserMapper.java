package com.silveira.care360.data.mapper;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.silveira.care360.data.local.entity.UserEntity;
import com.silveira.care360.data.remote.dto.UserDocumentDto;
import com.silveira.care360.domain.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper para transformar modelos de datos (Firebase/DTOs) a modelos de dominio.
 * Esto asegura que nuestra capa de dominio sea independiente de librerias externas.
 */
public class UserMapper {

    public static User fromEntity(UserEntity entity) {
        if (entity == null) return null;

        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getPhotoUrl(),
                entity.getActiveGroupId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isActive(),
                entity.getAuthProvider()
        );
    }

    public static UserEntity toEntity(User user) {
        if (user == null) return null;

        return new UserEntity(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPhotoUrl(),
                user.getActiveGroupId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.isActive(),
                user.getAuthProvider()
        );
    }

    public static User fromDto(UserDocumentDto dto) {
        if (dto == null) return null;

        return new User(
                dto.getId() != null ? dto.getId() : "",
                dto.getEmail(),
                dto.getDisplayName(),
                dto.getPhotoUrl(),
                dto.getActiveGroupId(),
                dto.getCreatedAt(),
                dto.getUpdatedAt(),
                dto.isActive(),
                dto.getAuthProvider()
        );
    }

    public static UserDocumentDto toDocumentDto(User user, String authProviderOverride) {
        if (user == null) return null;

        UserDocumentDto dto = new UserDocumentDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setActiveGroupId(user.getActiveGroupId());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setActive(user.isActive());
        dto.setAuthProvider(authProviderOverride != null ? authProviderOverride : user.getAuthProvider());
        return dto;
    }

    public static UserDocumentDto fromFirestore(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        UserDocumentDto dto = new UserDocumentDto();
        dto.setId(document.getId());
        dto.setEmail(document.getString("email"));
        dto.setDisplayName(document.getString("displayName"));
        dto.setPhotoUrl(document.getString("photoUrl"));
        dto.setActiveGroupId(document.getString("activeGroupId"));
        dto.setAuthProvider(document.getString("authProvider"));

        Long createdAt = readMillis(document, "createdAt");
        if (createdAt != null) dto.setCreatedAt(createdAt);

        Long updatedAt = readMillis(document, "updatedAt");
        if (updatedAt != null) dto.setUpdatedAt(updatedAt);

        Boolean active = document.getBoolean("active");
        dto.setActive(active == null || active);

        return dto;
    }

    private static Long readMillis(DocumentSnapshot document, String fieldName) {
        if (document == null || fieldName == null || fieldName.trim().isEmpty()) {
            return null;
        }

        Object value = document.get(fieldName);
        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) value).toDate().getTime();
        }

        return null;
    }

    public static Map<String, Object> toFirestoreMap(UserDocumentDto dto) {
        Map<String, Object> data = new HashMap<>();
        if (dto == null) {
            return data;
        }

        data.put("uid", dto.getId());
        data.put("email", dto.getEmail());
        data.put("displayName", dto.getDisplayName());
        data.put("photoUrl", dto.getPhotoUrl());
        data.put("activeGroupId", dto.getActiveGroupId());
        data.put("createdAt", dto.getCreatedAt());
        data.put("updatedAt", dto.getUpdatedAt());
        data.put("active", dto.isActive());
        data.put("authProvider", dto.getAuthProvider());
        return data;
    }

    public static Map<String, Object> toActiveGroupUpdateMap(String groupId, Object updatedAtValue) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("activeGroupId", groupId);
        updates.put("updatedAt", updatedAtValue);
        return updates;
    }

    public static Map<String, Object> toClearActiveGroupUpdateMap(Object updatedAtValue) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("activeGroupId", null);
        updates.put("updatedAt", updatedAtValue);
        return updates;
    }

    /**
     * Transforma un FirebaseUser a nuestro modelo de dominio User.
     */
    public static User fromFirebaseUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return null;

        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setDisplayName(firebaseUser.getDisplayName());
        user.setPhotoUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
        user.setActive(true);
        user.setCreatedAt(System.currentTimeMillis());
        user.setUpdatedAt(System.currentTimeMillis());

        if (firebaseUser.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo info : firebaseUser.getProviderData()) {
                if ("google.com".equals(info.getProviderId())) {
                    user.setAuthProvider("google");
                    break;
                }
                if ("password".equals(info.getProviderId())) {
                    user.setAuthProvider("password");
                }
            }
        }

        return user;
    }
}
