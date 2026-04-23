package com.silveira.care360.core.reminder;

import com.silveira.care360.domain.model.Medicamento;

public interface MedicationReminderScheduler {
    boolean canScheduleExactAlarms();
    void scheduleMedicamentoReminders(Medicamento medicamento);
    void cancelMedicamentoReminders(String medicamentoId);
}
