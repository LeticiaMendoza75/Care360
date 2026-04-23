package com.silveira.care360.data.mapper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.Timestamp;
import com.silveira.care360.data.remote.dto.SeguimientoRegistroDocumentDto;
import com.silveira.care360.domain.model.SeguimientoRegistro;

import java.util.HashMap;
import java.util.Map;

public final class SeguimientoRegistroMapper {

    private SeguimientoRegistroMapper() {
    }

    public static SeguimientoRegistro fromDto(SeguimientoRegistroDocumentDto dto) {
        if (dto == null) return null;
        return new SeguimientoRegistro(
                safe(dto.getId()),
                safe(dto.getTipo()),
                safe(dto.getValorPrincipal()),
                safe(dto.getValorSecundario()),
                safe(dto.getNotas()),
                dto.getRecordedAt() != null ? dto.getRecordedAt() : 0L,
                safe(dto.getCreatedBy()),
                dto.getCreatedAt() != null ? dto.getCreatedAt() : 0L,
                safe(dto.getUpdatedBy()),
                dto.getUpdatedAt() != null ? dto.getUpdatedAt() : 0L,
                dto.getDeleted() != null && dto.getDeleted()
        );
    }

    public static SeguimientoRegistroDocumentDto fromFirestore(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;
        SeguimientoRegistroDocumentDto dto = new SeguimientoRegistroDocumentDto();
        dto.setId(document.getId());
        dto.setTipo(document.getString("tipo"));
        dto.setValorPrincipal(document.getString("valorPrincipal"));
        dto.setValorSecundario(document.getString("valorSecundario"));
        dto.setNotas(document.getString("notas"));
        dto.setRecordedAt(readTimestamp(document.get("recordedAt")));
        dto.setCreatedBy(document.getString("createdBy"));
        dto.setCreatedAt(readTimestamp(document.get("createdAt")));
        dto.setUpdatedBy(document.getString("updatedBy"));
        dto.setUpdatedAt(readTimestamp(document.get("updatedAt")));
        dto.setDeleted(document.getBoolean("deleted"));
        return dto;
    }

    public static Map<String, Object> toCreateMap(SeguimientoRegistro registro) {
        Map<String, Object> data = new HashMap<>();
        data.put("tipo", safe(registro != null ? registro.getTipo() : null));
        data.put("valorPrincipal", safe(registro != null ? registro.getValorPrincipal() : null));
        data.put("valorSecundario", safe(registro != null ? registro.getValorSecundario() : null));
        data.put("notas", safe(registro != null ? registro.getNotas() : null));
        data.put("recordedAt", registro != null && registro.getRecordedAt() > 0 ? registro.getRecordedAt() : FieldValue.serverTimestamp());
        data.put("createdBy", safe(registro != null ? registro.getCreatedBy() : null));
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedBy", safe(registro != null ? registro.getUpdatedBy() : null));
        data.put("updatedAt", FieldValue.serverTimestamp());
        data.put("deleted", false);
        return data;
    }

    public static Map<String, Object> toUpdateMap(SeguimientoRegistro registro) {
        Map<String, Object> data = new HashMap<>();
        data.put("tipo", safe(registro != null ? registro.getTipo() : null));
        data.put("valorPrincipal", safe(registro != null ? registro.getValorPrincipal() : null));
        data.put("valorSecundario", safe(registro != null ? registro.getValorSecundario() : null));
        data.put("notas", safe(registro != null ? registro.getNotas() : null));
        data.put("recordedAt", registro != null && registro.getRecordedAt() > 0 ? registro.getRecordedAt() : FieldValue.serverTimestamp());
        data.put("updatedBy", safe(registro != null ? registro.getUpdatedBy() : null));
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
