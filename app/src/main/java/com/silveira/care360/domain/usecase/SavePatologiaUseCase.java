package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Patologia;
import com.silveira.care360.domain.repository.PatologiaRepository;

import javax.inject.Inject;

public class SavePatologiaUseCase {

    private final PatologiaRepository repository;

    @Inject
    public SavePatologiaUseCase(PatologiaRepository repository) {
        this.repository = repository;
    }

    public void execute(String groupId, Patologia patologia, ResultCallback<Void> callback) {
        repository.savePatologia(groupId, patologia, callback);
    }
}
