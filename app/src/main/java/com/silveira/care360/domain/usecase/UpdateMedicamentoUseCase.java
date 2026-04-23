package com.silveira.care360.domain.usecase;

import com.silveira.care360.core.reminder.MedicationReminderScheduler;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.repository.MedicamentoRepository;

import javax.inject.Inject;

public class UpdateMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicationReminderScheduler medicationReminderScheduler;

    @Inject
    public UpdateMedicamentoUseCase(MedicamentoRepository medicamentoRepository,
                                    MedicationReminderScheduler medicationReminderScheduler) {
        this.medicamentoRepository = medicamentoRepository;
        this.medicationReminderScheduler = medicationReminderScheduler;
    }

    public void execute(String activeGroupId,
                        String userId,
                        Medicamento medicamento,
                        ResultCallback<Result> callback) {
        if (medicamento == null) {
            callback.onError("Medicamento invalido");
            return;
        }

        medicamento.setUpdatedBy(userId);
        medicamento.setUpdatedAt(System.currentTimeMillis());
        if (isBlank(medicamento.getFechaFin())) {
            medicamento.setFechaFin("Indefinido");
        }
        if (medicamento.getObservaciones() == null) {
            medicamento.setObservaciones("");
        }

        medicamentoRepository.updateMedicamento(activeGroupId, medicamento, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                medicationReminderScheduler.cancelMedicamentoReminders(medicamento.getId());
                if (medicamento.isAlertasActivas()) {
                    if (!medicationReminderScheduler.canScheduleExactAlarms()) {
                        callback.onSuccess(Result.updatedRequiresExactAlarmPermission());
                        return;
                    }
                    medicationReminderScheduler.scheduleMedicamentoReminders(medicamento);
                }
                callback.onSuccess(Result.updated());
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

        public static Result updated() {
            return new Result(false);
        }

        public static Result updatedRequiresExactAlarmPermission() {
            return new Result(true);
        }

        public boolean isExactAlarmPermissionRequired() {
            return exactAlarmPermissionRequired;
        }
    }
}
