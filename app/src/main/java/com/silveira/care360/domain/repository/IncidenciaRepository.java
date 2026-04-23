package com.silveira.care360.domain.repository;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Incidencia;

import java.util.List;

public interface IncidenciaRepository {
    void getIncidenciasByGroupId(String groupId, ResultCallback<List<Incidencia>> callback);
    void addIncidencia(String groupId, Incidencia incidencia, ResultCallback<Void> callback);
    void deleteIncidencia(String groupId, String incidenciaId, ResultCallback<Void> callback);
}
