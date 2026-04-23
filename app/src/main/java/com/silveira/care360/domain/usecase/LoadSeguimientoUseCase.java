package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.SeguimientoRegistro;
import com.silveira.care360.domain.repository.SeguimientoRepository;

import java.util.List;

import javax.inject.Inject;

public class LoadSeguimientoUseCase {

    private final SeguimientoRepository repository;

    @Inject
    public LoadSeguimientoUseCase(SeguimientoRepository repository) {
        this.repository = repository;
    }

    public void execute(String groupId, ResultCallback<List<SeguimientoRegistro>> callback) {
        repository.loadRegistros(groupId, callback);
    }
}
