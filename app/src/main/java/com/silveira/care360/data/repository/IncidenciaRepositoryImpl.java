package com.silveira.care360.data.repository;

import com.silveira.care360.data.mapper.IncidenciaMapper;
import com.silveira.care360.data.remote.dto.IncidenciaDocumentDto;
import com.silveira.care360.data.remote.firestore.FirebaseIncidenciaDataSource;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Incidencia;
import com.silveira.care360.domain.repository.IncidenciaRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class IncidenciaRepositoryImpl implements IncidenciaRepository {

    private final FirebaseIncidenciaDataSource firebaseIncidenciaDataSource;

    @Inject
    public IncidenciaRepositoryImpl(FirebaseIncidenciaDataSource firebaseIncidenciaDataSource) {
        this.firebaseIncidenciaDataSource = firebaseIncidenciaDataSource;
    }

    @Override
    public void getIncidenciasByGroupId(String groupId, ResultCallback<List<Incidencia>> callback) {
        firebaseIncidenciaDataSource.getIncidenciasByGroupId(groupId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudieron cargar las incidencias");
                return;
            }
            List<Incidencia> incidencias = new ArrayList<>();
            List<IncidenciaDocumentDto> dtos = task.getResult();
            if (dtos != null) {
                for (IncidenciaDocumentDto dto : dtos) {
                    Incidencia incidencia = IncidenciaMapper.fromDto(dto);
                    if (incidencia != null) incidencias.add(incidencia);
                }
            }
            Collections.sort(incidencias, new Comparator<Incidencia>() {
                @Override
                public int compare(Incidencia left, Incidencia right) {
                    String leftFechaKey = IncidenciaMapper.buildFechaKey(left != null ? left.getFecha() : "");
                    String rightFechaKey = IncidenciaMapper.buildFechaKey(right != null ? right.getFecha() : "");
                    int dateCompare = rightFechaKey.compareTo(leftFechaKey);
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    String leftHora = normalizeHora(left != null ? left.getHora() : "");
                    String rightHora = normalizeHora(right != null ? right.getHora() : "");
                    int hourCompare = rightHora.compareTo(leftHora);
                    if (hourCompare != 0) {
                        return hourCompare;
                    }
                    long leftUpdatedAt = left != null ? left.getUpdatedAt() : 0L;
                    long rightUpdatedAt = right != null ? right.getUpdatedAt() : 0L;
                    return Long.compare(rightUpdatedAt, leftUpdatedAt);
                }
            });
            callback.onSuccess(incidencias);
        });
    }

    @Override
    public void addIncidencia(String groupId, Incidencia incidencia, ResultCallback<Void> callback) {
        firebaseIncidenciaDataSource.addIncidencia(groupId, IncidenciaMapper.toDto(incidencia)).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo guardar la incidencia");
                return;
            }
            callback.onSuccess(null);
        });
    }

    @Override
    public void deleteIncidencia(String groupId, String incidenciaId, ResultCallback<Void> callback) {
        firebaseIncidenciaDataSource.deleteIncidencia(groupId, incidenciaId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo eliminar la incidencia");
                return;
            }
            callback.onSuccess(null);
        });
    }

    private String normalizeHora(String hora) {
        if (hora == null || hora.trim().isEmpty()) {
            return "";
        }
        String value = hora.trim();
        String[] parts = value.split(":");
        if (parts.length != 2) {
            return value;
        }
        try {
            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());
            return String.format("%02d:%02d", hours, minutes);
        } catch (Exception ignored) {
            return value;
        }
    }
}
