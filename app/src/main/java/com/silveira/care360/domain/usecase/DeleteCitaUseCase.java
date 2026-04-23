package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.repository.CitaRepository;

import javax.inject.Inject;

public class DeleteCitaUseCase {

    private final CitaRepository citaRepository;

    @Inject
    public DeleteCitaUseCase(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public void execute(String activeGroupId, String citaId, ResultCallback<Void> callback) {
        citaRepository.deleteCita(activeGroupId, citaId, callback);
    }
}
