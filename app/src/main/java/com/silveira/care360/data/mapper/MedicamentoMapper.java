package com.silveira.care360.data.mapper;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.silveira.care360.data.remote.dto.DiaMedicacionDocumentDto;
import com.silveira.care360.data.remote.dto.MedicamentoDocumentDto;
import com.silveira.care360.domain.model.DiaMedicacion;
import com.silveira.care360.domain.model.Medicamento;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicamentoMapper {

    public static Medicamento fromDto(MedicamentoDocumentDto dto) {
        if (dto == null) {
            return null;
        }

        return new Medicamento(
                dto.getId(),
                dto.getNombre(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                dto.getObservaciones(),
                dto.isAlertasActivas(),
                fromDiaDtos(dto.getDias()),
                dto.getCreatedBy(),
                dto.getCreatedAt(),
                dto.getUpdatedBy(),
                dto.getUpdatedAt(),
                dto.isDeleted()
        );
    }

    public static MedicamentoDocumentDto toDocumentDto(Medicamento medicamento) {
        if (medicamento == null) {
            return null;
        }

        MedicamentoDocumentDto dto = new MedicamentoDocumentDto();
        dto.setId(medicamento.getId());
        dto.setNombre(medicamento.getNombre());
        dto.setFechaInicio(medicamento.getFechaInicio());
        dto.setFechaFin(medicamento.getFechaFin());
        dto.setObservaciones(medicamento.getObservaciones());
        dto.setAlertasActivas(medicamento.isAlertasActivas());
        dto.setDias(toDiaDtos(medicamento.getDias()));
        dto.setCreatedBy(medicamento.getCreatedBy());
        dto.setCreatedAt(medicamento.getCreatedAt());
        dto.setUpdatedBy(medicamento.getUpdatedBy());
        dto.setUpdatedAt(medicamento.getUpdatedAt());
        dto.setDeleted(medicamento.isDeleted());
        return dto;
    }

    public static MedicamentoDocumentDto fromFirestore(DocumentSnapshot doc, List<DiaMedicacionDocumentDto> dias) {
        if (doc == null || !doc.exists()) {
            return null;
        }

        MedicamentoDocumentDto dto = new MedicamentoDocumentDto();
        dto.setId(doc.getId());
        dto.setNombre(doc.getString("nombre"));
        dto.setFechaInicio(doc.getString("fechaInicio"));
        dto.setFechaFin(doc.getString("fechaFin"));
        dto.setObservaciones(doc.getString("observaciones"));
        Boolean alertasActivas = doc.getBoolean("alertasActivas");
        dto.setAlertasActivas(alertasActivas != null && alertasActivas);
        dto.setDias(dias);
        dto.setCreatedBy(doc.getString("createdBy"));
        dto.setUpdatedBy(doc.getString("updatedBy"));

        Timestamp createdAt = doc.getTimestamp("createdAt");
        Timestamp updatedAt = doc.getTimestamp("updatedAt");

        dto.setCreatedAt(createdAt != null ? createdAt.toDate().getTime() : 0L);
        dto.setUpdatedAt(updatedAt != null ? updatedAt.toDate().getTime() : 0L);
        Boolean deleted = doc.getBoolean("deleted");
        dto.setDeleted(deleted != null && deleted);
        return dto;
    }

    public static DiaMedicacionDocumentDto fromFirestoreDia(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            return null;
        }

        DiaMedicacionDocumentDto dto = new DiaMedicacionDocumentDto();
        dto.setFecha(doc.getString("fecha"));
        dto.setFechaKey(doc.getString("fechaKey"));

        List<String> horas = (List<String>) doc.get("horas");
        dto.setHoras(horas != null ? horas : new ArrayList<>());
        return dto;
    }

    public static Map<String, Object> toFirestoreMedicamentoMap(MedicamentoDocumentDto dto, String medicamentoId) {
        Map<String, Object> data = new HashMap<>();
        if (dto == null) {
            return data;
        }

        data.put("id", medicamentoId);
        data.put("nombre", dto.getNombre());
        data.put("fechaInicio", dto.getFechaInicio());
        data.put("fechaFin", dto.getFechaFin());
        data.put("observaciones", dto.getObservaciones());
        data.put("alertasActivas", dto.isAlertasActivas());
        data.put("createdBy", dto.getCreatedBy());
        data.put("createdAt", new Timestamp(new java.util.Date(dto.getCreatedAt())));
        data.put("updatedBy", dto.getUpdatedBy());
        data.put("updatedAt", new Timestamp(new java.util.Date(dto.getUpdatedAt())));
        data.put("deleted", dto.isDeleted());
        return data;
    }

    public static Map<String, Object> toFirestoreDiaMap(DiaMedicacionDocumentDto dto, String fechaKey) {
        Map<String, Object> data = new HashMap<>();
        if (dto == null) {
            return data;
        }

        data.put("fecha", dto.getFecha());
        data.put("fechaKey", fechaKey);
        data.put("horas", dto.getHoras() != null ? dto.getHoras() : new ArrayList<String>());
        return data;
    }

    private static List<DiaMedicacion> fromDiaDtos(List<DiaMedicacionDocumentDto> dtos) {
        List<DiaMedicacion> dias = new ArrayList<>();
        if (dtos == null) {
            return dias;
        }

        for (DiaMedicacionDocumentDto dto : dtos) {
            if (dto == null) {
                continue;
            }

            dias.add(new DiaMedicacion(
                    dto.getFecha(),
                    dto.getHoras() != null ? dto.getHoras() : new ArrayList<>()
            ));
        }

        return dias;
    }

    private static List<DiaMedicacionDocumentDto> toDiaDtos(List<DiaMedicacion> dias) {
        List<DiaMedicacionDocumentDto> dtos = new ArrayList<>();
        if (dias == null) {
            return dtos;
        }

        for (DiaMedicacion dia : dias) {
            if (dia == null) {
                continue;
            }

            DiaMedicacionDocumentDto dto = new DiaMedicacionDocumentDto();
            dto.setFecha(dia.getFecha());
            dto.setFechaKey(buildFechaKey(dia.getFecha()));
            dto.setHoras(dia.getHoras());
            dtos.add(dto);
        }

        return dtos;
    }

    private static String buildFechaKey(String fecha) {
        if (fecha == null) {
            return "";
        }

        String[] parts = fecha.split("/");
        if (parts.length == 3) {
            String dd = parts[0];
            String mm = parts[1];
            String yyyy = parts[2];
            return yyyy + "-" + mm + "-" + dd;
        }

        return fecha.replace("/", "-").trim();
    }
}
