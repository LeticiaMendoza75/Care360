package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Patologia;
import com.silveira.care360.domain.repository.PatologiaRepository;

import java.util.List;

import javax.inject.Inject;

public class LoadPatologiasUseCase {

    private final PatologiaRepository repository;

    @Inject
    public LoadPatologiasUseCase(PatologiaRepository repository) {
        this.repository = repository;
    }

    public void execute(String groupId, ResultCallback<List<Patologia>> callback) {
        repository.loadPatologias(groupId, callback);
    }
}
