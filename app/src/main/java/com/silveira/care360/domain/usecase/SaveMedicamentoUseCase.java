package com.silveira.care360.domain.usecase;

import com.silveira.care360.core.reminder.MedicationReminderScheduler;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.DiaMedicacion;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.repository.MedicamentoRepository;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

public class SaveMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicationReminderScheduler medicationReminderScheduler;

    @Inject
    public SaveMedicamentoUseCase(MedicamentoRepository medicamentoRepository,
                                  MedicationReminderScheduler medicationReminderScheduler) {
        this.medicamentoRepository = medicamentoRepository;
        this.medicationReminderScheduler = medicationReminderScheduler;
    }

    public void execute(String activeGroupId,
                        String userId,
                        String nombre,
                        String fechaInicio,
                        String fechaFin,
                        String observaciones,
                        boolean alertasActivas,
                        List<DiaMedicacion> dias,
                        ResultCallback<Result> callback) {

        String fechaFinFinal = isBlank(fechaFin) ? "Indefinido" : fechaFin.trim();
        String observacionesFinal = observaciones != null ? observaciones.trim() : "";
        long now = System.currentTimeMillis();

        Medicamento medicamento = new Medicamento(
                UUID.randomUUID().toString(),
                nombre != null ? nombre.trim() : null,
                fechaInicio != null ? fechaInicio.trim() : null,
                fechaFinFinal,
                observacionesFinal,
                alertasActivas,
                dias,
                userId,
                now,
                userId,
                now
        );

        medicamentoRepository.addMedicamento(activeGroupId, medicamento, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (medicamento.isAlertasActivas()) {
                    if (!medicationReminderScheduler.canScheduleExactAlarms()) {
                        callback.onSuccess(Result.savedRequiresExactAlarmPermission());
                        return;
                    }
                    medicationReminderScheduler.scheduleMedicamentoReminders(medicamento);
                } else {
                    medicationReminderScheduler.cancelMedicamentoReminders(medicamento.getId());
                }
                callback.onSuccess(Result.saved());
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class Result {
        private final boolean exactAlarmPermissionRequired;

        private Result(boolean exactAlarmPermissionRequired) {
            this.exactAlarmPermissionRequired = exactAlarmPermissionRequired;
        }

        public static Result saved() {
            return new Result(false);
        }

        public static Result savedRequiresExactAlarmPermission() {
            return new Result(true);
        }

        public boolean isExactAlarmPermissionRequired() {
            return exactAlarmPermissionRequired;
        }
    }
}
