package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.repository.IncidenciaRepository;

import javax.inject.Inject;

public class DeleteIncidenciaUseCase {

    private final IncidenciaRepository incidenciaRepository;

    @Inject
    public DeleteIncidenciaUseCase(IncidenciaRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    public void execute(String groupId, String incidenciaId, ResultCallback<Void> callback) {
        incidenciaRepository.deleteIncidencia(groupId, incidenciaId, callback);
    }
}
