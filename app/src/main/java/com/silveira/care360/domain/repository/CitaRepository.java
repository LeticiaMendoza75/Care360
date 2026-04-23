package com.silveira.care360.domain.repository;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;

import java.util.List;

public interface CitaRepository {

    void getCitasByGroupId(String groupId, ResultCallback<List<Cita>> callback);

    void addCita(String groupId, Cita cita, ResultCallback<Void> callback);

    void updateCita(String groupId, Cita cita, ResultCallback<Void> callback);

    void deleteCita(String groupId, String citaId, ResultCallback<Void> callback);
}
