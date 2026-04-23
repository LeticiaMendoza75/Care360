package com.silveira.care360.core.reminder;

import com.silveira.care360.domain.model.DiaMedicacion;
import com.silveira.care360.domain.model.Medicamento;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class MedicationReminderTimeCalculator {

    private static final String[] DATE_PATTERNS = {
            "dd/MM/yyyy",
            "d/M/yyyy",
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "d-M-yyyy"
    };

    private static final String[] TIME_PATTERNS = {
            "HH:mm",
            "H:mm",
            "hh:mm a",
            "h:mm a"
    };

    private MedicationReminderTimeCalculator() {
    }

    public static ReminderSlot findNextReminder(Medicamento medicamento) {
        if (medicamento == null) {
            return null;
        }

        ReminderSlot next = null;
        for (ReminderSlot slot : buildFutureSlots(medicamento, System.currentTimeMillis())) {
            if (next == null || slot.triggerAtMillis < next.triggerAtMillis) {
                next = slot;
            }
        }
        return next;
    }

    public static List<ReminderSlot> buildFutureSlots(Medicamento medicamento, long nowMillis) {
        List<ReminderSlot> slots = new ArrayList<>();
        if (medicamento == null || medicamento.getDias() == null) {
            return slots;
        }

        for (DiaMedicacion dia : medicamento.getDias()) {
            if (dia == null || dia.getHoras() == null) {
                continue;
            }

            for (String hora : dia.getHoras()) {
                long triggerAt = parseDateTime(dia.getFecha(), hora);
                if (triggerAt <= nowMillis) {
                    continue;
                }
                slots.add(new ReminderSlot(
                        medicamento.getId(),
                        safeText(medicamento.getNombre(), "Medicamento"),
                        safeText(dia.getFecha(), ""),
                        safeText(hora, ""),
                        triggerAt
                ));
            }
        }

        return slots;
    }

    public static String buildDisplayTime(ReminderSlot slot) {
        if (slot == null) {
            return "";
        }
        if (slot.hora == null || slot.hora.trim().isEmpty()) {
            return slot.fecha != null ? slot.fecha : "";
        }
        if (slot.fecha == null || slot.fecha.trim().isEmpty()) {
            return slot.hora;
        }
        return slot.fecha + " · " + slot.hora;
    }

    public static long parseDateWithoutHourForDisplay(String fecha, long nowMillis) {
        if (fecha == null) {
            return -1L;
        }

        String normalizedFecha = fecha.trim();
        for (String datePattern : DATE_PATTERNS) {
            SimpleDateFormat formatter = new SimpleDateFormat(datePattern, Locale.getDefault());
            formatter.setLenient(false);
            try {
                Date parsed = formatter.parse(normalizedFecha);
                if (parsed == null) {
                    continue;
                }
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.setTime(parsed);
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
                calendar.set(java.util.Calendar.MINUTE, 59);
                calendar.set(java.util.Calendar.SECOND, 59);
                calendar.set(java.util.Calendar.MILLISECOND, 999);
                long triggerAt = calendar.getTimeInMillis();
                if (triggerAt >= nowMillis) {
                    return triggerAt;
                }
            } catch (ParseException ignored) {
                // try next pattern
            }
        }
        return -1L;
    }

    private static long parseDateTime(String fecha, String hora) {
        if (fecha == null || hora == null) {
            return -1L;
        }

        String normalizedFecha = fecha.trim();
        String normalizedHora = hora.trim().toUpperCase(Locale.getDefault());

        for (String datePattern : DATE_PATTERNS) {
            for (String timePattern : TIME_PATTERNS) {
                Date parsed = tryParse(normalizedFecha, normalizedHora, datePattern, timePattern, Locale.getDefault());
                if (parsed == null) {
                    parsed = tryParse(normalizedFecha, normalizedHora, datePattern, timePattern, Locale.US);
                }
                if (parsed != null) {
                    return parsed.getTime();
                }
            }
        }

        return -1L;
    }

    private static Date tryParse(String fecha,
                                 String hora,
                                 String datePattern,
                                 String timePattern,
                                 Locale locale) {
        SimpleDateFormat formatter = new SimpleDateFormat(datePattern + " " + timePattern, locale);
        formatter.setLenient(false);
        try {
            return formatter.parse(fecha + " " + hora);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private static String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    public static final class ReminderSlot {
        public final String medicamentoId;
        public final String medicamentoNombre;
        public final String fecha;
        public final String hora;
        public final long triggerAtMillis;

        public ReminderSlot(String medicamentoId,
                            String medicamentoNombre,
                            String fecha,
                            String hora,
                            long triggerAtMillis) {
            this.medicamentoId = medicamentoId;
            this.medicamentoNombre = medicamentoNombre;
            this.fecha = fecha;
            this.hora = hora;
            this.triggerAtMillis = triggerAtMillis;
        }
    }
}
