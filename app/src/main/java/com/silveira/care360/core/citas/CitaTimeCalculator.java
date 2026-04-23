package com.silveira.care360.core.citas;

import com.silveira.care360.domain.model.Cita;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CitaTimeCalculator {

    private static final String[] DATE_PATTERNS = {
            "dd/MM/yyyy", "d/M/yyyy", "yyyy-MM-dd", "dd-MM-yyyy", "d-M-yyyy"
    };
    private static final String[] TIME_PATTERNS = {
            "HH:mm", "H:mm", "hh:mm a", "h:mm a"
    };

    private CitaTimeCalculator() {
    }

    public static long toMillis(Cita cita) {
        if (cita == null) return -1L;
        return parseDateTime(cita.getFecha(), cita.getHora());
    }

    public static String buildDisplayDateTime(Cita cita) {
        if (cita == null) return "";
        String fecha = safeText(cita.getFecha());
        String hora = safeText(cita.getHora());
        if (fecha.isEmpty()) return hora;
        if (hora.isEmpty()) return fecha;
        return fecha + " · " + hora;
    }

    private static long parseDateTime(String fecha, String hora) {
        if (fecha == null || hora == null) return -1L;
        String normalizedFecha = fecha.trim();
        String normalizedHora = hora.trim().toUpperCase(Locale.getDefault());

        for (String datePattern : DATE_PATTERNS) {
            for (String timePattern : TIME_PATTERNS) {
                Date parsed = tryParse(normalizedFecha, normalizedHora, datePattern, timePattern, Locale.getDefault());
                if (parsed == null) {
                    parsed = tryParse(normalizedFecha, normalizedHora, datePattern, timePattern, Locale.US);
                }
                if (parsed != null) return parsed.getTime();
            }
        }
        return -1L;
    }

    private static Date tryParse(String fecha, String hora, String datePattern, String timePattern, Locale locale) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(datePattern + " " + timePattern, locale);
            format.setLenient(false);
            return format.parse(fecha + " " + hora);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private static String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
