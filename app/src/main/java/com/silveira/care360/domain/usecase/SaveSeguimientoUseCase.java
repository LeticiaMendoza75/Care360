package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.SeguimientoRegistro;
import com.silveira.care360.domain.repository.SeguimientoRepository;

import javax.inject.Inject;

public class SaveSeguimientoUseCase {

    private final SeguimientoRepository repository;

    @Inject
    public SaveSeguimientoUseCase(SeguimientoRepository repository) {
        this.repository = repository;
    }

    public void execute(String groupId, SeguimientoRegistro registro, ResultCallback<Void> callback) {
        repository.saveRegistro(groupId, registro, callback);
    }
}
