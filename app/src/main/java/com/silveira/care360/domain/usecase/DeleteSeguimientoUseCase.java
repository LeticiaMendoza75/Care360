package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.repository.SeguimientoRepository;

import javax.inject.Inject;

public class DeleteSeguimientoUseCase {

    private final SeguimientoRepository repository;

    @Inject
    public DeleteSeguimientoUseCase(SeguimientoRepository repository) {
        this.repository = repository;
    }

    public void execute(String groupId, String registroId, String actorUserId, ResultCallback<Void> callback) {
        repository.deleteRegistro(groupId, registroId, actorUserId, callback);
    }
}
