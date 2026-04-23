package com.silveira.care360.data.mapper;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.silveira.care360.data.remote.dto.DocumentoDocumentDto;
import com.silveira.care360.domain.model.Documento;

import java.util.HashMap;
import java.util.Map;

public final class DocumentoMapper {

    private DocumentoMapper() {
    }

    public static Documento fromDto(DocumentoDocumentDto dto) {
        if (dto == null) return null;
        return new Documento(
                safeText(dto.getId()),
                safeText(dto.getTitulo()),
                safeText(dto.getTipo()),
                safeText(dto.getFechaDocumento()),
                safeText(dto.getNotas()),
                safeText(dto.getFileUrl()),
                safeText(dto.getFileName()),
                safeText(dto.getMimeType()),
                safeText(dto.getCreatedBy()),
                dto.getCreatedAt(),
                safeText(dto.getUpdatedBy()),
                dto.getUpdatedAt()
        );
    }

    public static DocumentoDocumentDto toDto(Documento documento) {
        if (documento == null) return null;
        return new DocumentoDocumentDto(
                safeText(documento.getId()),
                safeText(documento.getTitulo()),
                safeText(documento.getTipo()),
                safeText(documento.getFechaDocumento()),
                safeText(documento.getNotas()),
                safeText(documento.getFileUrl()),
                safeText(documento.getFileName()),
                safeText(documento.getMimeType()),
                safeText(documento.getCreatedBy()),
                documento.getCreatedAt(),
                safeText(documento.getUpdatedBy()),
                documento.getUpdatedAt()
        );
    }

    public static DocumentoDocumentDto fromFirestore(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        return new DocumentoDocumentDto(
                doc.getId(),
                safeText(doc.getString("titulo")),
                safeText(doc.getString("tipo")),
                safeText(doc.getString("fechaDocumento")),
                safeText(doc.getString("notas")),
                safeText(doc.getString("fileUrl")),
                safeText(doc.getString("fileName")),
                safeText(doc.getString("mimeType")),
                safeText(doc.getString("createdBy")),
                parseLong(doc.get("createdAt")),
                safeText(doc.getString("updatedBy")),
                parseLong(doc.get("updatedAt"))
        );
    }

    public static Map<String, Object> toFirestoreMap(DocumentoDocumentDto dto, String documentoId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", documentoId);
        map.put("titulo", safeText(dto.getTitulo()));
        map.put("tipo", safeText(dto.getTipo()));
        map.put("fechaDocumento", safeText(dto.getFechaDocumento()));
        map.put("notas", safeText(dto.getNotas()));
        map.put("fileUrl", safeText(dto.getFileUrl()));
        map.put("fileName", safeText(dto.getFileName()));
        map.put("mimeType", safeText(dto.getMimeType()));
        map.put("createdBy", safeText(dto.getCreatedBy()));
        map.put("createdAt", dto.getCreatedAt());
        map.put("updatedBy", safeText(dto.getUpdatedBy()));
        map.put("updatedAt", dto.getUpdatedAt());
        return map;
    }

    private static long parseLong(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof Timestamp) return ((Timestamp) value).toDate().getTime();
        return 0L;
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
