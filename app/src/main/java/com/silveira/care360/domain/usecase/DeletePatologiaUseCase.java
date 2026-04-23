package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.repository.PatologiaRepository;

import javax.inject.Inject;

public class DeletePatologiaUseCase {

    private final PatologiaRepository repository;

    @Inject
    public DeletePatologiaUseCase(PatologiaRepository repository) {
        this.repository = repository;
    }

    public void execute(String groupId, String patologiaId, String actorUserId, ResultCallback<Void> callback) {
        repository.deletePatologia(groupId, patologiaId, actorUserId, callback);
    }
}
