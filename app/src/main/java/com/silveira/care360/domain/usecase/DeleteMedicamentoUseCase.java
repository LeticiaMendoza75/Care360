package com.silveira.care360.domain.usecase;

import com.silveira.care360.core.reminder.MedicationReminderScheduler;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.repository.MedicamentoRepository;

import javax.inject.Inject;

public class DeleteMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicationReminderScheduler medicationReminderScheduler;

    @Inject
    public DeleteMedicamentoUseCase(MedicamentoRepository medicamentoRepository,
                                    MedicationReminderScheduler medicationReminderScheduler) {
        this.medicamentoRepository = medicamentoRepository;
        this.medicationReminderScheduler = medicationReminderScheduler;
    }

    public void execute(String activeGroupId, String medicamentoId, String deletedByUserId, ResultCallback<Void> callback) {
        medicamentoRepository.deleteMedicamento(activeGroupId, medicamentoId, deletedByUserId, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                medicationReminderScheduler.cancelMedicamentoReminders(medicamentoId);
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
