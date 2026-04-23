package com.silveira.care360.domain.usecase;

import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.repository.MedicamentoRepository;
import com.silveira.care360.domain.repository.UserRepository;

import javax.inject.Inject;

public class LoadMedicamentoDetalleUseCase {

    private final UserRepository userRepository;
    private final MedicamentoRepository medicamentoRepository;

    @Inject
    public LoadMedicamentoDetalleUseCase(UserRepository userRepository,
                                         MedicamentoRepository medicamentoRepository) {
        this.userRepository = userRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    public void execute(String userId, String medicamentoId, ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String groupId) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    callback.onError("No hay grupo activo");
                    return;
                }

                medicamentoRepository.getMedicamentoById(groupId, medicamentoId, new ResultCallback<Medicamento>() {
                    @Override
                    public void onSuccess(Medicamento medicamento) {
                        if (medicamento == null || medicamento.isDeleted()) {
                            callback.onError("Medicamento no encontrado");
                            return;
                        }

                        callback.onSuccess(new Result(groupId, medicamento));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("No se pudo cargar el medicamento");
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError("No se pudo obtener el grupo activo");
            }
        });
    }

    public static class Result {
        private final String activeGroupId;
        private final Medicamento medicamento;

        public Result(String activeGroupId, Medicamento medicamento) {
            this.activeGroupId = activeGroupId;
            this.medicamento = medicamento;
        }

        public String getActiveGroupId() {
            return activeGroupId;
        }

        public Medicamento getMedicamento() {
            return medicamento;
        }
    }
}
