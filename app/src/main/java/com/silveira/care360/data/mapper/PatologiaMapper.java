package com.silveira.care360.data.mapper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.Timestamp;
import com.silveira.care360.data.remote.dto.PatologiaDocumentDto;
import com.silveira.care360.domain.model.Patologia;

import java.util.HashMap;
import java.util.Map;

public final class PatologiaMapper {

    private PatologiaMapper() {
    }

    public static Patologia fromDto(PatologiaDocumentDto dto) {
        if (dto == null) return null;
        return new Patologia(
                safe(dto.getId()),
                safe(dto.getNombre()),
                safe(dto.getDescripcion()),
                safe(dto.getCreatedBy()),
                dto.getCreatedAt() != null ? dto.getCreatedAt() : 0L,
                safe(dto.getUpdatedBy()),
                dto.getUpdatedAt() != null ? dto.getUpdatedAt() : 0L,
                dto.getDeleted() != null && dto.getDeleted()
        );
    }

    public static PatologiaDocumentDto fromFirestore(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;
        PatologiaDocumentDto dto = new PatologiaDocumentDto();
        dto.setId(document.getId());
        dto.setNombre(document.getString("nombre"));
        dto.setDescripcion(document.getString("descripcion"));
        dto.setCreatedBy(document.getString("createdBy"));
        dto.setCreatedAt(readTimestamp(document.get("createdAt")));
        dto.setUpdatedBy(document.getString("updatedBy"));
        dto.setUpdatedAt(readTimestamp(document.get("updatedAt")));
        dto.setDeleted(document.getBoolean("deleted"));
        return dto;
    }

    public static Map<String, Object> toCreateMap(Patologia patologia) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", safe(patologia != null ? patologia.getNombre() : null));
        data.put("descripcion", safe(patologia != null ? patologia.getDescripcion() : null));
        data.put("createdBy", safe(patologia != null ? patologia.getCreatedBy() : null));
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedBy", safe(patologia != null ? patologia.getUpdatedBy() : null));
        data.put("updatedAt", FieldValue.serverTimestamp());
        data.put("deleted", false);
        return data;
    }

    public static Map<String, Object> toUpdateMap(Patologia patologia) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", safe(patologia != null ? patologia.getNombre() : null));
        data.put("descripcion", safe(patologia != null ? patologia.getDescripcion() : null));
        data.put("updatedBy", safe(patologia != null ? patologia.getUpdatedBy() : null));
        data.put("updatedAt", FieldValue.serverTimestamp());
        return data;
    }

    public static Map<String, Object> toSoftDeleteMap(String actorUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", true);
        data.put("updatedBy", safe(actorUserId));
        data.put("updatedAt", FieldValue.serverTimestamp());
        return data;
    }

    private static long readTimestamp(Object value) {
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().getTime();
        if (value instanceof Long) return (Long) value;
        return 0L;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
