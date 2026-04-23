package com.silveira.care360.data.mapper;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.silveira.care360.data.remote.dto.CitaDocumentDto;
import com.silveira.care360.domain.model.Cita;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class CitaMapper {

    private CitaMapper() {
    }

    public static Cita fromDto(CitaDocumentDto dto) {
        if (dto == null) return null;
        return new Cita(
                safeText(dto.getId()),
                safeText(dto.getTitulo()),
                safeText(dto.getFecha()),
                safeText(dto.getHora()),
                safeText(dto.getLugar()),
                safeText(dto.getProfesional()),
                safeText(dto.getPersonaEncargada()),
                safeText(dto.getObservaciones()),
                dto.isRecordatorioActivo(),
                safeText(dto.getCreatedBy()),
                dto.getCreatedAt(),
                safeText(dto.getUpdatedBy()),
                dto.getUpdatedAt()
        );
    }

    public static CitaDocumentDto toDto(Cita cita) {
        if (cita == null) return null;
        return new CitaDocumentDto(
                safeText(cita.getId()),
                safeText(cita.getTitulo()),
                safeText(cita.getFecha()),
                buildFechaKey(cita.getFecha()),
                safeText(cita.getHora()),
                safeText(cita.getLugar()),
                safeText(cita.getProfesional()),
                safeText(cita.getPersonaEncargada()),
                safeText(cita.getObservaciones()),
                cita.isRecordatorioActivo(),
                safeText(cita.getCreatedBy()),
                cita.getCreatedAt(),
                safeText(cita.getUpdatedBy()),
                cita.getUpdatedAt()
        );
    }

    public static CitaDocumentDto fromFirestore(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        return new CitaDocumentDto(
                doc.getId(),
                safeText(doc.getString("titulo")),
                safeText(doc.getString("fecha")),
                safeText(doc.getString("fechaKey")),
                safeText(doc.getString("hora")),
                safeText(doc.getString("lugar")),
                safeText(doc.getString("profesional")),
                safeText(doc.getString("personaEncargada")),
                safeText(doc.getString("observaciones")),
                Boolean.TRUE.equals(doc.getBoolean("recordatorioActivo")),
                safeText(doc.getString("createdBy")),
                parseLong(doc.get("createdAt")),
                safeText(doc.getString("updatedBy")),
                parseLong(doc.get("updatedAt"))
        );
    }

    public static Map<String, Object> toFirestoreMap(CitaDocumentDto dto, String citaId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", citaId);
        map.put("titulo", safeText(dto.getTitulo()));
        map.put("fecha", safeText(dto.getFecha()));
        map.put("fechaKey", buildFechaKey(dto.getFecha()));
        map.put("hora", safeText(dto.getHora()));
        map.put("lugar", safeText(dto.getLugar()));
        map.put("profesional", safeText(dto.getProfesional()));
        map.put("personaEncargada", safeText(dto.getPersonaEncargada()));
        map.put("observaciones", safeText(dto.getObservaciones()));
        map.put("recordatorioActivo", dto.isRecordatorioActivo());
        map.put("createdBy", safeText(dto.getCreatedBy()));
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedBy", safeText(dto.getUpdatedBy()));
        map.put("updatedAt", dto.getUpdatedAt());
        return map;
    }

    public static String buildFechaKey(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) return "";
        String normalized = fecha.trim();
        String[] slashParts = normalized.split("/");
        if (slashParts.length == 3) {
            return String.format(Locale.getDefault(), "%s-%02d-%02d",
                    slashParts[2], parseIntSafe(slashParts[1]), parseIntSafe(slashParts[0]));
        }
        String[] dashParts = normalized.split("-");
        if (dashParts.length == 3 && dashParts[0].length() == 4) return normalized;
        return normalized.replace("/", "-");
    }

    private static long parseLong(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().getTime();
        return 0L;
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
