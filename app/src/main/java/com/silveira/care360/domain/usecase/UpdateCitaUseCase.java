package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.domain.repository.CitaRepository;

import javax.inject.Inject;

public class UpdateCitaUseCase {

    private final CitaRepository citaRepository;

    @Inject
    public UpdateCitaUseCase(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public void execute(String activeGroupId, String currentUserId, Cita cita, ResultCallback<Void> callback) {
        if (cita == null) {
            callback.onError("Cita invalida");
            return;
        }
        cita.setUpdatedBy(currentUserId);
        cita.setUpdatedAt(System.currentTimeMillis());
        citaRepository.updateCita(activeGroupId, cita, callback);
    }
}
