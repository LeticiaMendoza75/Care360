package com.silveira.care360.data.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.silveira.care360.R;
import com.silveira.care360.ui.HomeActivity;

public class MedicationReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "medication_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }

        String medicamentoId = intent.getStringExtra(HomeActivity.EXTRA_ALERT_MEDICATION_ID);
        String medicamentoNombre = intent.getStringExtra(HomeActivity.EXTRA_ALERT_MEDICATION_NAME);
        String hora = intent.getStringExtra(HomeActivity.EXTRA_ALERT_MEDICATION_TIME);
        String fecha = intent.getStringExtra(HomeActivity.EXTRA_ALERT_MEDICATION_DATE);

        createChannelIfNeeded(context);

        Intent openIntent = new Intent(context, HomeActivity.class);
        openIntent.putExtra(HomeActivity.EXTRA_SHOW_MEDICATION_ALERT, true);
        openIntent.putExtra(HomeActivity.EXTRA_ALERT_MEDICATION_ID, medicamentoId);
        openIntent.putExtra(HomeActivity.EXTRA_ALERT_MEDICATION_NAME, medicamentoNombre);
        openIntent.putExtra(HomeActivity.EXTRA_ALERT_MEDICATION_TIME, hora);
        openIntent.putExtra(HomeActivity.EXTRA_ALERT_MEDICATION_DATE, fecha);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                buildRequestCode(medicamentoId, fecha, hora),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Recordatorio de medicación";
        String content = buildContentText(medicamentoNombre, hora, fecha);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medica)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setSound(getAlarmSoundUri())
                .setVibrate(new long[]{0, 500, 300, 500})
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        try {
            NotificationManagerCompat.from(context).notify(
                    buildRequestCode(medicamentoId, fecha, hora),
                    builder.build()
            );
        } catch (SecurityException ignored) {
        }
    }

    private void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de medicación",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 300, 500});
        channel.setSound(
                getAlarmSoundUri(),
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
        );
        channel.setDescription("Avisos de próximas tomas de medicación");
        manager.createNotificationChannel(channel);
    }

    private Uri getAlarmSoundUri() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri != null) {
            return alarmUri;
        }
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    private String buildContentText(String nombre, String hora, String fecha) {
        StringBuilder builder = new StringBuilder();
        builder.append(nombre != null && !nombre.trim().isEmpty() ? nombre : "Toca medicación");
        if (hora != null && !hora.trim().isEmpty()) {
            builder.append(" · ").append(hora);
        }
        if (fecha != null && !fecha.trim().isEmpty()) {
            builder.append("\n").append(fecha);
        }
        return builder.toString();
    }

    private int buildRequestCode(String medicamentoId, String fecha, String hora) {
        return (String.valueOf(medicamentoId) + "|" + String.valueOf(fecha) + "|" + String.valueOf(hora)).hashCode();
    }
}
