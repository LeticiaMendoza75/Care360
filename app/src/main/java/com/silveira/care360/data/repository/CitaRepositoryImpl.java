package com.silveira.care360.data.repository;

import com.silveira.care360.data.mapper.CitaMapper;
import com.silveira.care360.data.remote.dto.CitaDocumentDto;
import com.silveira.care360.data.remote.firestore.FirebaseCitaDataSource;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.domain.repository.CitaRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class CitaRepositoryImpl implements CitaRepository {

    private final FirebaseCitaDataSource firebaseCitaDataSource;

    @Inject
    public CitaRepositoryImpl(FirebaseCitaDataSource firebaseCitaDataSource) {
        this.firebaseCitaDataSource = firebaseCitaDataSource;
    }

    @Override
    public void getCitasByGroupId(String groupId, ResultCallback<List<Cita>> callback) {
        firebaseCitaDataSource.getCitasByGroupId(groupId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudieron cargar las citas");
                return;
            }

            List<Cita> citas = new ArrayList<>();
            List<CitaDocumentDto> dtos = task.getResult();
            if (dtos != null) {
                for (CitaDocumentDto dto : dtos) {
                    Cita cita = CitaMapper.fromDto(dto);
                    if (cita != null) {
                        citas.add(cita);
                    }
                }
            }
            callback.onSuccess(citas);
        });
    }

    @Override
    public void addCita(String groupId, Cita cita, ResultCallback<Void> callback) {
        firebaseCitaDataSource.addCita(groupId, CitaMapper.toDto(cita)).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo guardar la cita");
                return;
            }
            callback.onSuccess(null);
        });
    }

    @Override
    public void updateCita(String groupId, Cita cita, ResultCallback<Void> callback) {
        firebaseCitaDataSource.updateCita(groupId, CitaMapper.toDto(cita)).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo actualizar la cita");
                return;
            }
            callback.onSuccess(null);
        });
    }

    @Override
    public void deleteCita(String groupId, String citaId, ResultCallback<Void> callback) {
        firebaseCitaDataSource.deleteCita(groupId, citaId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                callback.onError("No se pudo eliminar la cita");
                return;
            }
            callback.onSuccess(null);
        });
    }
}
