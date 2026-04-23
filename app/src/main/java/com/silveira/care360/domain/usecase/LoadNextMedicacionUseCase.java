package com.silveira.care360.domain.usecase;

import com.silveira.care360.core.reminder.MedicationReminderTimeCalculator;
import com.silveira.care360.domain.common.ResultCallback;
import com.silveira.care360.domain.model.DiaMedicacion;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.domain.repository.MedicamentoRepository;
import com.silveira.care360.domain.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

public class LoadNextMedicacionUseCase {

    private final UserRepository userRepository;
    private final MedicamentoRepository medicamentoRepository;

    @Inject
    public LoadNextMedicacionUseCase(UserRepository userRepository,
                                     MedicamentoRepository medicamentoRepository) {
        this.userRepository = userRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    public void execute(String userId, ResultCallback<Result> callback) {
        userRepository.getActiveGroupId(userId, new ResultCallback<String>() {
            @Override
            public void onSuccess(String groupId) {
                if (groupId == null || groupId.trim().isEmpty()) {
                    callback.onSuccess(Result.empty());
                    return;
                }

                medicamentoRepository.getMedicamentosByGroupId(groupId, new ResultCallback<List<Medicamento>>() {
                    @Override
                    public void onSuccess(List<Medicamento> medicamentos) {
                        MedicationReminderTimeCalculator.ReminderSlot nextSlot = null;
                        Medicamento nextMedicamento = null;

                        if (medicamentos != null) {
                            for (Medicamento medicamento : medicamentos) {
                                if (medicamento == null || medicamento.isDeleted()) {
                                    continue;
                                }
                                MedicationReminderTimeCalculator.ReminderSlot candidate =
                                        MedicationReminderTimeCalculator.findNextReminder(medicamento);
                                if (candidate == null) {
                                    candidate = findNextScheduledDayWithoutHour(medicamento);
                                    if (candidate == null) {
                                        continue;
                                    }
                                }
                                if (nextSlot == null || candidate.triggerAtMillis < nextSlot.triggerAtMillis) {
                                    nextSlot = candidate;
                                    nextMedicamento = medicamento;
                                }
                            }
                        }

                        if (nextSlot == null || nextMedicamento == null) {
                            callback.onSuccess(Result.empty());
                            return;
                        }

                        callback.onSuccess(new Result(
                                nextMedicamento.getNombre(),
                                MedicationReminderTimeCalculator.buildDisplayTime(nextSlot),
                                nextMedicamento.isAlertasActivas()
                        ));
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("No se pudo cargar la próxima medicación");
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
        private final String nombre;
        private final String horario;
        private final boolean alertasActivas;

        public Result(String nombre, String horario, boolean alertasActivas) {
            this.nombre = nombre;
            this.horario = horario;
            this.alertasActivas = alertasActivas;
        }

        public static Result empty() {
            return new Result("", "", false);
        }

        public String getNombre() {
            return nombre;
        }

        public String getHorario() {
            return horario;
        }

        public boolean isAlertasActivas() {
            return alertasActivas;
        }
    }

    private MedicationReminderTimeCalculator.ReminderSlot findNextScheduledDayWithoutHour(Medicamento medicamento) {
        if (medicamento == null || medicamento.getDias() == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        MedicationReminderTimeCalculator.ReminderSlot next = null;
        for (DiaMedicacion dia : medicamento.getDias()) {
            if (dia == null || isBlank(dia.getFecha())) {
                continue;
            }
            boolean hasHours = dia.getHoras() != null && !dia.getHoras().isEmpty();
            if (hasHours) {
                continue;
            }
            long triggerAt = MedicationReminderTimeCalculator.parseDateWithoutHourForDisplay(dia.getFecha(), now);
            if (triggerAt <= 0L) {
                continue;
            }
            MedicationReminderTimeCalculator.ReminderSlot candidate = new MedicationReminderTimeCalculator.ReminderSlot(
                    medicamento.getId(),
                    medicamento.getNombre(),
                    dia.getFecha().trim(),
                    "",
                    triggerAt
            );
            if (next == null || candidate.triggerAtMillis < next.triggerAtMillis) {
                next = candidate;
            }
        }
        return next;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
