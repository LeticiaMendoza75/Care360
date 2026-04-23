package com.silveira.care360.data.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.silveira.care360.core.reminder.MedicationReminderScheduler;
import com.silveira.care360.core.reminder.MedicationReminderTimeCalculator;
import com.silveira.care360.domain.model.Medicamento;
import com.silveira.care360.ui.HomeActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class MedicationReminderSchedulerImpl implements MedicationReminderScheduler {

    private static final String PREFS_NAME = "medication_reminders";

    private final Context context;
    private final AlarmManager alarmManager;
    private final SharedPreferences preferences;

    @Inject
    public MedicationReminderSchedulerImpl(@ApplicationContext Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean canScheduleExactAlarms() {
        if (alarmManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }

        return true;
    }

    @Override
    public void scheduleMedicamentoReminders(Medicamento medicamento) {
        if (medicamento == null || isBlank(medicamento.getId())) {
            return;
        }

        cancelMedicamentoReminders(medicamento.getId());

        if (!medicamento.isAlertasActivas()) {
            return;
        }

        if (!canScheduleExactAlarms()) {
            return;
        }

        List<Integer> requestCodes = new ArrayList<>();
        for (MedicationReminderTimeCalculator.ReminderSlot slot :
                MedicationReminderTimeCalculator.buildFutureSlots(medicamento, System.currentTimeMillis())) {
            int requestCode = buildRequestCode(slot.medicamentoId, slot.fecha, slot.hora);
            PendingIntent operationIntent = buildOperationIntent(slot, requestCode);

            if (alarmManager != null) {
                try {
                    scheduleAlarm(slot.triggerAtMillis, operationIntent);
                    requestCodes.add(requestCode);
                } catch (SecurityException ignored) {
                    // Do not break medicamento save/update flows when the device
                    // does not grant exact-alarm privileges.
                }
            }
        }

        persistRequestCodes(medicamento.getId(), requestCodes);
    }

    @Override
    public void cancelMedicamentoReminders(String medicamentoId) {
        if (isBlank(medicamentoId) || alarmManager == null) {
            return;
        }

        for (Integer requestCode : loadRequestCodes(medicamentoId)) {
            alarmManager.cancel(buildCancelIntent(requestCode));
        }
        preferences.edit().remove(buildPrefsKey(medicamentoId)).apply();
    }

    private PendingIntent buildOperationIntent(MedicationReminderTimeCalculator.ReminderSlot slot, int requestCode) {
        Intent intent = new Intent(context, MedicationReminderReceiver.class);
        intent.putExtra(HomeActivity.EXTRA_SHOW_MEDICATION_ALERT, true);
        intent.putExtra(HomeActivity.EXTRA_ALERT_MEDICATION_ID, slot.medicamentoId);
        intent.putExtra(HomeActivity.EXTRA_ALERT_MEDICATION_NAME, slot.medicamentoNombre);
        intent.putExtra(HomeActivity.EXTRA_ALERT_MEDICATION_TIME, slot.hora);
        intent.putExtra(HomeActivity.EXTRA_ALERT_MEDICATION_DATE, slot.fecha);
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void scheduleAlarm(long triggerAtMillis, PendingIntent operationIntent) {
        if (alarmManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    operationIntent
            );
            return;
        }

        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                operationIntent
        );
    }

    private PendingIntent buildCancelIntent(int requestCode) {
        Intent intent = new Intent(context, MedicationReminderReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private int buildRequestCode(String medicamentoId, String fecha, String hora) {
        return (medicamentoId + "|" + fecha + "|" + hora).hashCode();
    }

    private void persistRequestCodes(String medicamentoId, List<Integer> requestCodes) {
        StringBuilder raw = new StringBuilder();
        for (int i = 0; i < requestCodes.size(); i++) {
            if (i > 0) {
                raw.append(",");
            }
            raw.append(requestCodes.get(i));
        }
        preferences.edit().putString(buildPrefsKey(medicamentoId), raw.toString()).apply();
    }

    private List<Integer> loadRequestCodes(String medicamentoId) {
        String raw = preferences.getString(buildPrefsKey(medicamentoId), "");
        List<Integer> result = new ArrayList<>();
        if (isBlank(raw)) {
            return result;
        }

        for (String part : raw.split(",")) {
            try {
                result.add(Integer.parseInt(part));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    private String buildPrefsKey(String medicamentoId) {
        return "medicamento_" + medicamentoId;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
