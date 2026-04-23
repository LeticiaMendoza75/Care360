package com.silveira.care360.data.mapper;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.silveira.care360.data.remote.dto.IncidenciaDocumentDto;
import com.silveira.care360.domain.model.Incidencia;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class IncidenciaMapper {

    private IncidenciaMapper() {
    }

    public static Incidencia fromDto(IncidenciaDocumentDto dto) {
        if (dto == null) return null;
        return new Incidencia(
                safe(dto.getId()),
                safe(dto.getTipo()),
                safe(dto.getFecha()),
                safe(dto.getHora()),
                safe(dto.getNivel()),
                safe(dto.getDescripcion()),
                safe(dto.getCreatedBy()),
                dto.getCreatedAt(),
                safe(dto.getUpdatedBy()),
                dto.getUpdatedAt()
        );
    }

    public static IncidenciaDocumentDto toDto(Incidencia incidencia) {
        if (incidencia == null) return null;
        return new IncidenciaDocumentDto(
                safe(incidencia.getId()),
                safe(incidencia.getTipo()),
                safe(incidencia.getFecha()),
                buildFechaKey(incidencia.getFecha()),
                safe(incidencia.getHora()),
                safe(incidencia.getNivel()),
                safe(incidencia.getDescripcion()),
                safe(incidencia.getCreatedBy()),
                incidencia.getCreatedAt(),
                safe(incidencia.getUpdatedBy()),
                incidencia.getUpdatedAt()
        );
    }

    public static IncidenciaDocumentDto fromFirestore(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        return new IncidenciaDocumentDto(
                doc.getId(),
                safe(doc.getString("tipo")),
                safe(doc.getString("fecha")),
                safe(doc.getString("fechaKey")),
                safe(doc.getString("hora")),
                safe(doc.getString("nivel")),
                safe(doc.getString("descripcion")),
                safe(doc.getString("createdBy")),
                parseLong(doc.get("createdAt")),
                safe(doc.getString("updatedBy")),
                parseLong(doc.get("updatedAt"))
        );
    }

    public static Map<String, Object> toFirestoreMap(IncidenciaDocumentDto dto, String id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("tipo", safe(dto.getTipo()));
        map.put("fecha", safe(dto.getFecha()));
        map.put("fechaKey", buildFechaKey(dto.getFecha()));
        map.put("hora", safe(dto.getHora()));
        map.put("nivel", safe(dto.getNivel()));
        map.put("descripcion", safe(dto.getDescripcion()));
        map.put("createdBy", safe(dto.getCreatedBy()));
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedBy", safe(dto.getUpdatedBy()));
        map.put("updatedAt", dto.getUpdatedAt());
        return map;
    }

    private static long parseLong(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().getTime();
        return 0L;
    }

    public static String buildFechaKey(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) return "";
        String normalized = fecha.trim();
        String[] slashParts = normalized.split("/");
        if (slashParts.length == 3) {
            return String.format(Locale.getDefault(), "%s-%02d-%02d",
                    slashParts[2], parseIntSafe(slashParts[1]), parseIntSafe(slashParts[0]));
        }
        return normalized.replace("/", "-");
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
