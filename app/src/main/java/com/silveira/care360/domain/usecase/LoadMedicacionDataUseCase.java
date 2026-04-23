package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.repository.MedicamentoRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class LoadMedicacionDataUseCase {

    private final UserRepository userRepository;
    private final MedicamentoRepository medicamentoRepository;

    @Inject
    public LoadMedicacionDataUseCase(UserRepository userRepository,
                                     MedicamentoRepository medicamentoRepository) {
        this.userRepository = userRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    public void execute(String userId, ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String groupId) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    callback.onError("No hay grupo activo");
                    return;
                }

                medicamentoRepository.getMedicamentosByGroupId(groupId, new ResultCallback<List<Medicamento>>() {
                    @Override
                    public void onSuccess(List<Medicamento> medicamentos) {
                        List<Medicamento> visibles = new ArrayList<>();
                        if (medicamentos != null) {
                            for (Medicamento medicamento : medicamentos) {
                                if (medicamento != null && !medicamento.isDeleted()) {
                                    visibles.add(medicamento);
                                }
                            }
                        }
                        callback.onSuccess(new Result(
                                groupId,
                                visibles
                        ));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("No se pudieron cargar los medicamentos");
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
        private final List<Medicamento> medicamentos;

        public Result(String activeGroupId, List<Medicamento> medicamentos) {
            this.activeGroupId = activeGroupId;
            this.medicamentos = medicamentos != null ? medicamentos : new ArrayList<>();
        }

        public String getActiveGroupId() {
            return activeGroupId;
        }

        public List<Medicamento> getMedicamentos() {
            return medicamentos;
        }
    }
}
