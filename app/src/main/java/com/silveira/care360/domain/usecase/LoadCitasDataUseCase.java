package com.silveira.care360.domain.usecase;

import com.silveira.care360.core.citas.CitaTimeCalculator;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Cita;
import com.silveira.care360.domain.repository.CitaRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class LoadCitasDataUseCase {

    private final UserRepository userRepository;
    private final CitaRepository citaRepository;

    @Inject
    public LoadCitasDataUseCase(UserRepository userRepository, CitaRepository citaRepository) {
        this.userRepository = userRepository;
        this.citaRepository = citaRepository;
    }

    public void execute(String userId, ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String groupId) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    callback.onError("No hay grupo activo");
                    return;
                }

                citaRepository.getCitasByGroupId(groupId, new ResultCallback<List<Cita>>() {
                    @Override
                    public void onSuccess(List<Cita> citas) {
                        List<Cita> sorted = new ArrayList<>(citas != null ? citas : new ArrayList<>());
                        Collections.sort(sorted, Comparator.comparingLong(CitaTimeCalculator::toMillis));
                        callback.onSuccess(new Result(groupId, sorted));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("No se pudieron cargar las citas");
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError("No se pudo cargar el grupo activo");
            }
        });
    }

    public static class Result {
        private final String activeGroupId;
        private final List<Cita> citas;

        public Result(String activeGroupId, List<Cita> citas) {
            this.activeGroupId = activeGroupId;
            this.citas = citas != null ? citas : new ArrayList<>();
        }

        public String getActiveGroupId() { return activeGroupId; }
        public List<Cita> getCitas() { return citas; }
    }
}
