package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Incidencia;
import com.silveira.care360.domain.repository.IncidenciaRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class LoadIncidenciasUseCase {

    private final UserRepository userRepository;
    private final IncidenciaRepository incidenciaRepository;

    @Inject
    public LoadIncidenciasUseCase(UserRepository userRepository,
                                  IncidenciaRepository incidenciaRepository) {
        this.userRepository = userRepository;
        this.incidenciaRepository = incidenciaRepository;
    }

    public void execute(String userId, ResultCallback<List<Incidencia>> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String groupId) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }
                incidenciaRepository.getIncidenciasByGroupId(groupId, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
