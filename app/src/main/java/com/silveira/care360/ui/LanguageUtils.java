package com.silveira.care360.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageUtils {

    public static final String EXTRA_LANGUAGE_REFRESH = "extra_language_refresh";

    public static void setLocale(Activity activity, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        activity.getBaseContext().createConfigurationContext(config);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Reiniciar la actividad para aplicar los cambios
        Intent intent = activity.getIntent();
        intent.putExtra(EXTRA_LANGUAGE_REFRESH, true);
        activity.finish();
        activity.startActivity(intent);
    }
}
